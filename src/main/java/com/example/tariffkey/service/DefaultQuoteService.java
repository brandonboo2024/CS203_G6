package com.example.tariffkey.service;

import com.example.tariffkey.exception.TariffNotFoundException;
import com.example.tariffkey.model.*;
import com.example.tariffkey.repository.FeeScheduleRepository;
import com.example.tariffkey.repository.ProductRepository;
import com.example.tariffkey.repository.TariffRepository;
import com.example.tariffkey.repository.WitsTariffRepository;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class DefaultQuoteService {

    private final ProductRepository productRepository;
    private final FeeScheduleRepository feeScheduleRepository;
    private final TariffRepository tariffRepository;
    private final WitsTariffRepository witsTariffRepository;

    @org.springframework.beans.factory.annotation.Autowired
    public DefaultQuoteService(ProductRepository productRepository,
                               FeeScheduleRepository feeScheduleRepository,
                               TariffRepository tariffRepository,
                               WitsTariffRepository witsTariffRepository) {
        this.productRepository = productRepository;
        this.feeScheduleRepository = feeScheduleRepository;
        this.tariffRepository = tariffRepository;
        this.witsTariffRepository = witsTariffRepository;
    }

    public TariffApiResponse fetchQuote(TariffApiRequest request) {
        return fetchQuote(request, null, null);
    }

    public TariffApiResponse fetchQuote(TariffApiRequest request, LocalDate windowStart, LocalDate windowEnd) {
        String originCountry = requireCode(request.getOriginCountry(), "origin country");
        String destinationCountry = requireCode(request.getDestCountry(), "destination country");
        String productCode = requireCode(request.getHs6(), "product");
        String yearValue = trimToNull(request.getYear());
        Integer requestedYear = parseYear(yearValue);

        LocalDate effectiveStart = windowStart != null ? windowStart : LocalDate.now();
        LocalDate effectiveEnd = windowEnd != null ? windowEnd : effectiveStart;
        if (effectiveStart.isAfter(effectiveEnd)) {
            LocalDate swap = effectiveStart;
            effectiveStart = effectiveEnd;
            effectiveEnd = swap;
        }

        Tariff adminTariff = resolveAdminTariff(originCountry, destinationCountry, productCode, effectiveStart, effectiveEnd);
        if (adminTariff != null) {
            return TariffApiResponse.builder()
                    .tariffRate(adminTariff.getRate())
                    .httpStatus(200)
                    .fromCache(true)
                    .label(adminTariff.getLabel())
                    .sourceLabel("admin:" + adminTariff.getId())
                    .notes(adminTariff.getNotes())
                    .validFrom(adminTariff.getValidFrom())
                    .validTo(adminTariff.getValidTo())
                    .adminTariffId(adminTariff.getId())
                    .build();
        }

        Optional<WitsTariff> fromDataset = requestedYear == null
                ? Optional.empty()
                : witsTariffRepository.findFirstByReporterIsoAndPartnerCodeAndProductCodeAndYearOrderByIdAsc(
                originCountry, destinationCountry, productCode, requestedYear);

        if (fromDataset.isEmpty()) {
            fromDataset = witsTariffRepository.findFirstByReporterIsoAndPartnerCodeAndProductCodeOrderByYearDesc(
                    originCountry, destinationCountry, productCode);
        }

        WitsTariff match = fromDataset.orElseThrow(() ->
                new TariffNotFoundException("No tariff data found for the selected combination"));

        double rateDecimal = percentageToRate(match.getSimpleAverage());

        return TariffApiResponse.builder()
                .httpStatus(200)
                .tariffRate(rateDecimal)
                .tariffTypes(match.getEstCode() == null ? new String[0] : new String[]{match.getEstCode()})
                .year(match.getYear())
                .nomenclature(match.getNomenCode())
                .label("Dataset tariff rate")
                .sourceLabel(match.getNomenCode())
                .fromCache(true)
                .url("wits_tariffs:" + match.getSourceFile())
                .body("Lookup from imported dataset")
                .build();
    }

  private String requireCode(String value, String fieldName) {
      String normalized = trimToNull(value);
      if (normalized == null) {
          throw new IllegalArgumentException("Missing " + fieldName + " code");
      }
      return normalized;
  }

  private Integer parseYear(String value) {
      if (value == null || value.equalsIgnoreCase("ALL")) {
          return null;
      }
      try {
          return Integer.parseInt(value);
      } catch (NumberFormatException ex) {
          return null;
      }
  }

  private static String trimToNull(String value) {
      if (value == null) {
          return null;
      }
      String trimmed = value.trim();
      return trimmed.isEmpty() ? null : trimmed;
  }

    public TariffResponse calculateQuote(TariffRequest request) {
        if (request.getQuantity() == null || request.getQuantity() <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero");
        }

        Product product = resolveProduct(request);
        String hsCode = resolveHsCode(request, product);

        TariffApiRequest apiRequest = TariffApiRequest.builder()
                .originCountry(requireCode(request.getFromCountry(), "origin country"))
                .destCountry(requireCode(request.getToCountry(), "destination country"))
                .hs6(requireCode(hsCode, "product code"))
                .year(resolveYear(request))
                .build();

        LocalDate windowStart = toLocalDate(request.getCalculationFrom());
        LocalDate windowEnd = toLocalDate(request.getCalculationTo());
        TariffApiResponse apiResponse = fetchQuote(apiRequest, windowStart, windowEnd);
        if (apiResponse.getHttpStatus() >= 400) {
            throw new IllegalStateException("Failed to retrieve tariff data from imported dataset");
        }

        Double storedBasePrice = product != null ? product.getBasePrice() : null;
        Double confirmedBasePrice = request.getCustomBasePrice();
        if (confirmedBasePrice == null) {
            return buildPriceRequiredResponse(request, hsCode, apiResponse, storedBasePrice);
        }

        double basePrice = confirmedBasePrice;
        boolean pricePersisted = false;
        if (product == null) {
            product = persistProductIfMissing(request, hsCode, basePrice);
            pricePersisted = true;
        }

        double itemPrice = basePrice * request.getQuantity();
        double rateDecimal = apiResponse.getTariffRate();
        double tariffAmount = itemPrice * rateDecimal;

        double handlingFee = request.isHandling() ? feeAmount("handling") : 0.0;
        double inspectionFee = request.isInspection() ? feeAmount("inspection") : 0.0;
        double processingFee = request.isProcessing() ? feeAmount("processing") : 0.0;
        double otherFees = request.isOthers() ? feeAmount("others") : 0.0;

        TariffResponse response = new TariffResponse();
        response.setItemPrice(itemPrice);
        response.setTariffRate(rateDecimal * 100.0);
        response.setTariffAmount(tariffAmount);
        response.setHandlingFee(handlingFee);
        response.setInspectionFee(inspectionFee);
        response.setProcessingFee(processingFee);
        response.setOtherFees(otherFees);
        response.setTotalPrice(itemPrice + tariffAmount + handlingFee + inspectionFee + processingFee + otherFees);
        response.setSegments(new ArrayList<>());
        response.setLabel(StringUtils.hasText(apiResponse.getLabel())
                ? apiResponse.getLabel()
                : apiResponse.isFromCache() ? "Dataset tariff rate" : "Average tariff rate (WITS)");
        response.setSource(StringUtils.hasText(apiResponse.getSourceLabel())
                ? apiResponse.getSourceLabel()
                : apiResponse.getNomenclature());
        response.setAdminTariffId(apiResponse.getAdminTariffId());
        response.setValidFrom(formatLocalDate(apiResponse.getValidFrom()));
        response.setValidTo(formatLocalDate(apiResponse.getValidTo()));
        response.setNotes(apiResponse.getNotes());
        response.setPricePersisted(pricePersisted);
        return response;
    }

    private TariffResponse buildPriceRequiredResponse(TariffRequest request, String hsCode, TariffApiResponse apiResponse, Double suggestedBasePrice) {
        TariffResponse response = new TariffResponse();
        response.setPriceRequired(true);
        response.setMissingProduct(request.getProduct());
        response.setMissingHsCode(hsCode);
        response.setSegments(new ArrayList<>());
        response.setTariffRate(apiResponse.getTariffRate() * 100.0);
        response.setLabel(StringUtils.hasText(apiResponse.getLabel())
                ? apiResponse.getLabel()
                : "Tariff rate available");
        response.setSource(StringUtils.hasText(apiResponse.getSourceLabel())
                ? apiResponse.getSourceLabel()
                : apiResponse.getNomenclature());
        response.setAdminTariffId(apiResponse.getAdminTariffId());
        response.setValidFrom(formatLocalDate(apiResponse.getValidFrom()));
        response.setValidTo(formatLocalDate(apiResponse.getValidTo()));
        response.setNotes(apiResponse.getNotes());
        response.setMessage("Base price required for HS " + hsCode + " before totals can be calculated.");
        response.setSuggestedBasePrice(suggestedBasePrice);
        return response;
    }

    private String resolveYear(TariffRequest request) {
        String calcTo = request.getCalculationTo();
        String calcFrom = request.getCalculationFrom();
        String reference = calcTo != null && !calcTo.isBlank() ? calcTo : calcFrom;
        if (reference == null || reference.isBlank()) {
            return "ALL";
        }
        try {
            return String.valueOf(OffsetDateTime.parse(reference).getYear());
        } catch (Exception e) {
            return "ALL";
        }
    }

    private Tariff resolveAdminTariff(String originCountry, String destinationCountry, String productCode,
                                      LocalDate windowStart, LocalDate windowEnd) {
        List<Tariff> overlapping = tariffRepository.findActiveTariffs(
                originCountry, destinationCountry, productCode, windowStart, windowEnd);
        if (!overlapping.isEmpty()) {
            return overlapping.get(0);
        }
        return tariffRepository
                .findTopByOriginCountryAndDestinationCountryAndProductOrderByValidFromDesc(
                        originCountry, destinationCountry, productCode)
                .orElse(null);
    }

    private LocalDate toLocalDate(String iso) {
        if (!StringUtils.hasText(iso)) {
            return null;
        }
        try {
            return OffsetDateTime.parse(iso).toLocalDate();
        } catch (Exception e) {
            return null;
        }
    }

    private String formatLocalDate(LocalDate date) {
        return date == null ? null : date.toString();
    }

    private Product resolveProduct(TariffRequest request) {
        String requestedCode = trimToNull(request.getProduct());
        if (requestedCode != null) {
            Optional<Product> byCode = productRepository.findByCode(requestedCode);
            if (byCode.isPresent()) {
                return byCode.get();
            }
        }

        String hsCode = trimToNull(request.getHsCode());
        if (hsCode != null) {
            Optional<Product> byHs = productRepository.findByHsCode(hsCode);
            if (byHs.isPresent()) {
                return byHs.get();
            }
        }

        if (requestedCode != null && looksLikeHsCode(requestedCode)) {
            return productRepository.findByHsCode(requestedCode).orElse(null);
        }
        return null;
    }

    private String resolveHsCode(TariffRequest request, Product product) {
        String hsCode = trimToNull(request.getHsCode());
        if (hsCode != null) {
            return hsCode;
        }
        if (product != null) {
            return product.getHsCode();
        }
        String fallback = trimToNull(request.getProduct());
        if (looksLikeHsCode(fallback)) {
            return fallback;
        }
        throw new IllegalArgumentException("HS product code is required for tariff lookup");
    }

    private Product persistProductIfMissing(TariffRequest request, String hsCode, double basePrice) {
        String code = trimToNull(request.getProduct());
        if (code == null) {
            code = hsCode;
        }
        if (looksLikeHsCode(code)) {
            code = "hs_" + hsCode;
        }
        Product newProduct = Product.builder()
                .code(code)
                .hsCode(hsCode)
                .basePrice(basePrice)
                .build();
        return productRepository.save(newProduct);
    }

    private double feeAmount(String code) {
        return feeScheduleRepository.findById(code)
                .map(FeeSchedule::getAmount)
                .map(BigDecimal::doubleValue)
                .orElse(0.0);
    }

    private double percentageToRate(BigDecimal percentage) {
        if (percentage == null) {
            return 0.0;
        }
        return percentage.doubleValue() / 100.0;
    }

    private boolean looksLikeHsCode(String value) {
        String normalized = trimToNull(value);
        return normalized != null && normalized.matches("^[0-9]{4,10}$");
    }
}
