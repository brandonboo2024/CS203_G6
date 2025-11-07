package com.example.tariffkey.service;

import com.example.tariffkey.exception.TariffNotFoundException;
import com.example.tariffkey.model.*;
import com.example.tariffkey.repository.FeeScheduleRepository;
import com.example.tariffkey.repository.ProductRepository;
import com.example.tariffkey.repository.TariffRepository;
import com.example.tariffkey.repository.WitsTariffRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
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
        String originCountry = requireCode(request.getOriginCountry(), "origin country");
        String destinationCountry = requireCode(request.getDestCountry(), "destination country");
        String productCode = requireCode(request.getHs6(), "product");
        String yearValue = trimToNull(request.getYear());
        Integer requestedYear = parseYear(yearValue);

        Optional<Tariff> cachedTariff = tariffRepository.findByOriginCountryAndDestinationCountryAndProduct(
                originCountry, destinationCountry, productCode);

        if (cachedTariff.isPresent()) {
            Tariff tariff = cachedTariff.get();
            return TariffApiResponse.builder()
                    .tariffRate(tariff.getRate())
                    .httpStatus(200)
                    .fromCache(true)
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
        if (request.getQuantity() <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero");
        }

        Product product = productRepository.findByCode(request.getProduct())
                .orElseThrow(() -> new IllegalArgumentException("Unknown product: " + request.getProduct()));

        TariffApiRequest apiRequest = TariffApiRequest.builder()
                .originCountry(requireCode(request.getFromCountry(), "origin country"))
                .destCountry(requireCode(request.getToCountry(), "destination country"))
                .hs6(requireCode(product.getHsCode(), "product code"))
                .year(resolveYear(request))
                .build();

        TariffApiResponse apiResponse = fetchQuote(apiRequest);
        if (apiResponse.getHttpStatus() >= 400) {
            throw new IllegalStateException("Failed to retrieve tariff data from imported dataset");
        }

        double itemPrice = product.getBasePrice() * request.getQuantity();
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
        response.setLabel(apiResponse.isFromCache() ? "Dataset tariff rate" : "Average tariff rate (WITS)");
        response.setSource(apiResponse.getNomenclature());
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
}
