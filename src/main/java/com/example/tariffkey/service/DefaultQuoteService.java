package com.example.tariffkey.service;

import com.example.tariffkey.model.*;
import com.example.tariffkey.repository.FeeScheduleRepository;
import com.example.tariffkey.repository.ProductRepository;
import com.example.tariffkey.repository.TariffRepository; // Added
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

@Service
public class DefaultQuoteService {

    private static final ObjectMapper OM = new ObjectMapper();
    private static final Map<String, String> COUNTRY_CODE_MAP = buildCountryCodeMap();
    private static final Map<String, String> PRODUCT_CODE_MAP = buildProductCodeMap();

    private final ProductRepository productRepository;
    private final FeeScheduleRepository feeScheduleRepository;
    private final TariffRepository tariffRepository; // added repo to check db before making sql call
    private final HttpClient httpClient;
    private final String apiBaseUrl;

    @org.springframework.beans.factory.annotation.Autowired
    public DefaultQuoteService(
            ProductRepository productRepository,
            FeeScheduleRepository feeScheduleRepository,
            TariffRepository tariffRepository, // Added
            @Value("${wits.api.base-url:https://wits.worldbank.org/API/V1/SDMX/V21/datasource/TRN}") String apiBaseUrl
    ) {
        this(productRepository, feeScheduleRepository, tariffRepository, // Added
                HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(20)).build(),
                apiBaseUrl);
    }

    DefaultQuoteService(ProductRepository productRepository,
                        FeeScheduleRepository feeScheduleRepository,
                        TariffRepository tariffRepository, // Added
                        HttpClient httpClient,
                        String apiBaseUrl) {
        this.productRepository = productRepository;
        this.feeScheduleRepository = feeScheduleRepository;
        this.tariffRepository = tariffRepository; // Added
        this.httpClient = httpClient;
        this.apiBaseUrl = apiBaseUrl.endsWith("/") ? apiBaseUrl.substring(0, apiBaseUrl.length() - 1) : apiBaseUrl;
    }

    public TariffApiResponse fetchQuote(TariffApiRequest request) {
        String originCountry = requireCountryCode(request.getOriginCountry(), "origin country");
        String destinationCountry = requireCountryCode(request.getDestCountry(), "destination country");
        String productCode = requireProductCode(request.getHs6());
        String year = normalizeYear(request.getYear());

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

        String url = apiBaseUrl
                + "/reporter/" + enc(originCountry)
                + "/partner/" + enc(destinationCountry)
                + "/product/" + enc(productCode)
                + "/year/" + enc(year)
                + "/datatype/reported"
                + "?format=JSON";

        System.out.println("YOU URL IS THIS" + url);

        try {
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(60))
                    .header("Accept", "application/json")
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            Parsed parsed = parseSdmx(response.body());

            // Save to database
            Tariff newTariff = Tariff.builder()
                    .originCountry(originCountry)
                    .destinationCountry(destinationCountry)
                    .product(productCode)
                    .rate(parsed.rateDecimal())
                    .build();
            tariffRepository.save(newTariff);

            return TariffApiResponse.builder()
                    .url(url)
                    .httpStatus(response.statusCode())
                    .body(response.body())
                    .tariffRate(parsed.rateDecimal)
                    .tariffTypes(parsed.tariffTypes)
                    .year(parsed.year)
                    .nomenclature(parsed.nomenCode)
                    .fromCache(false) // Added
                    .build();
        } catch (Exception e) {
            TariffApiResponse out = new TariffApiResponse();
            out.setUrl(url);
            out.setHttpStatus(500);
            out.setBody("{\"error\":\"" + safe(e.getMessage()) + "\"}");
            return out;
        }
    }

  /** tiny holder for parsed pieces */
  private record Parsed(double rateDecimal, String[] tariffTypes, Integer year, String nomenCode) {
    static Parsed empty() { return new Parsed(0.0, new String[0], null, null); }
  }


  private String resolveCountryToCountryCode(String country) {
      String normalized = trimToNull(country);
      if (normalized == null) {
          return null;
      }
      if (isDigits(normalized, 3, 3)) {
          return normalized;
      }
      return COUNTRY_CODE_MAP.get(normalized.toUpperCase(Locale.ROOT));
  }

  private String resolveProductToProductCode(String product){
      String normalized = trimToNull(product);
      if (normalized == null) {
          return null;
      }
      if (isDigits(normalized, 4, 10) || normalized.contains("-") || normalized.contains("_")) {
          return normalized;
      }
      return PRODUCT_CODE_MAP.get(normalized.toLowerCase(Locale.ROOT));
  }

  private String requireCountryCode(String value, String fieldName) {
      String resolved = resolveCountryToCountryCode(value);
      if (resolved == null) {
          throw new IllegalArgumentException("Unknown " + fieldName + ": " + value);
      }
      return resolved;
  }

  private String requireProductCode(String value) {
      String resolved = resolveProductToProductCode(value);
      if (resolved == null) {
          throw new IllegalArgumentException("Unknown product code: " + value);
      }
      return resolved;
  }

  private String normalizeYear(String year) {
      String normalized = trimToNull(year);
      return normalized == null ? "ALL" : normalized;
  }

  private static String trimToNull(String value) {
      if (value == null) {
          return null;
      }
      String trimmed = value.trim();
      return trimmed.isEmpty() ? null : trimmed;
  }

  private static boolean isDigits(String value, int minLen, int maxLen) {
      int len = value.length();
      if (len < minLen || len > maxLen) {
          return false;
      }
      for (int i = 0; i < len; i++) {
          if (!Character.isDigit(value.charAt(i))) {
              return false;
          }
      }
      return true;
  }

  private static Map<String, String> buildCountryCodeMap() {
      Map<String, String> codes = new HashMap<>();
      addCountryCodes(codes, "702", "SG", "SINGAPORE");
      addCountryCodes(codes, "840", "US", "UNITED STATES");
      addCountryCodes(codes, "458", "MY", "MALAYSIA");
      addCountryCodes(codes, "764", "TH", "THAILAND");
      addCountryCodes(codes, "704", "VN", "VIETNAM");
      addCountryCodes(codes, "360", "ID", "INDONESIA");
      addCountryCodes(codes, "608", "PH", "PHILIPPINES");
      addCountryCodes(codes, "410", "KR", "SOUTH KOREA");
      addCountryCodes(codes, "356", "IN", "INDIA");
      addCountryCodes(codes, "036", "AU", "AUSTRALIA");
      addCountryCodes(codes, "826", "GB", "UNITED KINGDOM");
      addCountryCodes(codes, "276", "DE", "GERMANY");
      addCountryCodes(codes, "250", "FR", "FRANCE");
      addCountryCodes(codes, "380", "IT", "ITALY");
      addCountryCodes(codes, "724", "ES", "SPAIN");
      addCountryCodes(codes, "124", "CA", "CANADA");
      addCountryCodes(codes, "076", "BR", "BRAZIL");
      addCountryCodes(codes, "484", "MX", "MEXICO");
      addCountryCodes(codes, "643", "RU", "RUSSIA");
      addCountryCodes(codes, "710", "ZA", "SOUTH AFRICA");
      addCountryCodes(codes, "156", "CN", "CHINA");
      addCountryCodes(codes, "392", "JP", "JAPAN");
      return Collections.unmodifiableMap(codes);
  }

  private static void addCountryCodes(Map<String, String> target, String code, String... aliases) {
      for (String alias : aliases) {
          target.put(alias.toUpperCase(Locale.ROOT), code);
      }
  }

  private static Map<String, String> buildProductCodeMap() {
      Map<String, String> codes = new HashMap<>();
      addProductCode(codes, "electronics", "84-85_MachElec");
      addProductCode(codes, "clothing", "50-63_TextCloth");
      addProductCode(codes, "furniture", "UNCTAD-SoP3");
      addProductCode(codes, "food", "16-24_FoodProd");
      addProductCode(codes, "tools", "manuf");
      addProductCode(codes, "beauty", "UNCTAD-SoP3");
      addProductCode(codes, "beauty products", "UNCTAD-SoP3");
      addProductCode(codes, "sports", "64-67_Footwear");
      addProductCode(codes, "sports equipment", "64-67_Footwear");
      addProductCode(codes, "automotive", "Transp");
      addProductCode(codes, "automotive parts", "Transp");
      addProductCode(codes, "chem", "28-38_Chemicals");
      addProductCode(codes, "chemicals", "28-38_Chemicals");
      addProductCode(codes, "plastic or rubber", "39-40_PlastiRub");
      addProductCode(codes, "plastic , rubber", "39-40_PlastiRub");
      addProductCode(codes, "misc", "90-99_Miscellan");
      addProductCode(codes, "miscallaneous", "90-99_Miscellan");
      return Collections.unmodifiableMap(codes);
  }

  private static void addProductCode(Map<String, String> target, String alias, String code) {
      target.put(alias.toLowerCase(Locale.ROOT), code);
  }



    public TariffResponse calculateQuote(TariffRequest request) {
        if (request.getQuantity() <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero");
        }

        Product product = productRepository.findByCode(request.getProduct())
                .orElseThrow(() -> new IllegalArgumentException("Unknown product: " + request.getProduct()));

        TariffApiRequest apiRequest = TariffApiRequest.builder()
                .originCountry(requireCountryCode(request.getFromCountry(), "origin country"))
                .destCountry(requireCountryCode(request.getToCountry(), "destination country"))
                .hs6(requireProductCode(product.getHsCode()))
                .year(resolveYear(request))
                .build();

        TariffApiResponse apiResponse = fetchQuote(apiRequest);
        if (apiResponse.getHttpStatus() >= 400) {
            throw new IllegalStateException("Failed to retrieve tariff data from WITS API");
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
        response.setLabel(apiResponse.isFromCache() ? "Cached tariff rate" : "Average tariff rate (WITS)"); // added for checking
        response.setSource(apiResponse.getNomenclature());
        return response;
    }

    private static String enc(String s) {
        return s == null ? "" : URLEncoder.encode(s.trim(), StandardCharsets.UTF_8);
    }

    private static String safe(String s) {
        return s == null ? "" : s.replace("\"", "'");
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

    private static Parsed parseSdmx(String json) throws Exception {
        JsonNode root = OM.readTree(json);

        JsonNode seriesNode = root.path("dataSets").path(0).path("series");
        if (seriesNode.isMissingNode() || !seriesNode.fieldNames().hasNext()) {
            return Parsed.empty();
        }
        String firstSeriesKey = seriesNode.fieldNames().next();
        JsonNode observations = seriesNode.path(firstSeriesKey).path("observations");
        if (observations.isMissingNode() || !observations.fieldNames().hasNext()) {
            return Parsed.empty();
        }

        JsonNode timeVals = root.path("structure").path("dimensions").path("observation").get(0).path("values");
        String bestObsIdx = null;
        int bestYear = Integer.MIN_VALUE;
        Iterator<String> it = observations.fieldNames();
        while (it.hasNext()) {
            String idx = it.next();
            int year = parseIntSafe(timeVals.path(Integer.parseInt(idx)).path("id").asText());
            if (year > bestYear) {
                bestYear = year;
                bestObsIdx = idx;
            }
        }
        if (bestObsIdx == null) return Parsed.empty();

        double simpleAveragePercent = observations.path(bestObsIdx).path(0).asDouble(Double.NaN);
        double rateDecimal = Double.isNaN(simpleAveragePercent) ? 0.0 : simpleAveragePercent / 100.0;

        JsonNode obsAttrs = root.path("structure").path("attributes").path("observation");
        String nomenCode = valueByIdSingle(obsAttrs, "NOMENCODE");
        String tariffType = valueByIdSingle(obsAttrs, "TARIFFTYPE");
        String[] tariffTypes = (tariffType == null || tariffType.isBlank())
                ? new String[0]
                : new String[]{tariffType};

        return new Parsed(rateDecimal, tariffTypes, bestYear, nomenCode);
    }

    private static int parseIntSafe(String s) {
        try {
            return Integer.parseInt(s);
        } catch (Exception e) {
            return Integer.MIN_VALUE;
        }
    }

    private static String valueByIdSingle(JsonNode attrs, String id) {
        if (attrs == null || !attrs.isArray()) return null;
        for (JsonNode a : attrs) {
            if (id.equals(a.path("id").asText())) {
                JsonNode vals = a.path("values");
                if (vals.isArray() && vals.size() > 0) {
                    return vals.get(0).path("id").asText(null);
                }
            }
        }
        return null;
    }

    // private record Parsed(double rateDecimal, String[] tariffTypes, Integer year, String nomenCode) {
    //     static Parsed empty() {
    //         return new Parsed(0.0, new String[0], null, null);
    //     }
    // }
}
