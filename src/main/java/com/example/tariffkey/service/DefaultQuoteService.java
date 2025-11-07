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
<<<<<<< HEAD
=======
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
>>>>>>> brandon

@Service
public class DefaultQuoteService {

    private static final ObjectMapper OM = new ObjectMapper();

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
        // Check local database first
        Optional<Tariff> cachedTariff = tariffRepository.findByOriginCountryAndDestinationCountryAndProduct(
                request.getOriginCountry(), request.getDestCountry(), request.getHs6());

        if (cachedTariff.isPresent()) {
            Tariff tariff = cachedTariff.get();
            return TariffApiResponse.builder()
                    .tariffRate(tariff.getRate())
                    .fromCache(true)
                    .build();
        }

        // If not in DB, fetch from API
        // String reporter = enc(request.getOriginCountry());
        String reporter = "840";
        String partner = "000";
        String product = "020110";
        String year = "2020";
        // String partner = enc(request.getDestCountry());
        // String product = enc(request.getHs6());
        // String year = enc(request.getYear());

        String url = apiBaseUrl
                + "/reporter/" + reporter
                + "/partner/" + partner
                + "/product/" + product
                + "/year/" + year
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
                    .originCountry(request.getOriginCountry())
                    .destinationCountry(request.getDestCountry())
                    .product(request.getHs6())
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


  private String resolveCountryToCountryCode(String Country){
      Map<String, String> memberCode = new HashMap<>();
{
    memberCode.put("SG", "702");
    memberCode.put("Singapore" , "702");

    memberCode.put("US" , "840");
    memberCode.put("United States" , "840");

    memberCode.put("MY" , "458");
    memberCode.put("Malaysia" , "458");

    memberCode.put("TH" , "764");
    memberCode.put("Thailand" , "764");

    memberCode.put("VN" , "704");
    memberCode.put("Vietnam" , "704");

    memberCode.put("ID" , "360");
    memberCode.put("Indonesia" , "360");

    memberCode.put("PH" , "608");
    memberCode.put("Philippines" , "608");

    memberCode.put("KR" , "410");
    memberCode.put("South Korea" , "410");

    memberCode.put("IN" , "356");
    memberCode.put("India" , "356");

    memberCode.put("AU" , "036");
    memberCode.put("Australia" , "036");

    memberCode.put("GB" , "826");
    memberCode.put("United Kingdom" , "826");

    memberCode.put("DE" , "276"); //European Union
    memberCode.put("Germany" , "276"); //European Union -> ECS
    memberCode.put("FR" , "250");//European Union , can't find the "german" and "france" Econnomies in website, doesnt exist
    memberCode.put("France" , "250");// EU
    memberCode.put("IT" , "380"); //EU
    memberCode.put("Italy" , "380"); //EU
    memberCode.put("ES" , "724"); //EU
    memberCode.put("Spain" , "724"); //EU

    memberCode.put("CA" , "124");
    memberCode.put("Canada" , "124");

    memberCode.put("BR" , "076");
    memberCode.put("Brazil" , "076");

    memberCode.put("MX" , "484");
    memberCode.put("Mexico" , "484");

    memberCode.put("RU" , "643");
    memberCode.put("Russia" , "643");

    memberCode.put("ZA" , "710");
    memberCode.put("South Africa" , "710");

    memberCode.put("CN" , "156");
    memberCode.put("China" , "156");

    memberCode.put("JP" , "392");
    memberCode.put("Japan" , "392");

}

    String countryCode = memberCode.get(Country);

    return countryCode;
  }

  private String resolveProductToProductCode(String product){

Map<String,String> productCode = new HashMap<>();
    {
        productCode.put("electronics" , "854231");//Processors and controllers, whether or not combined with memories, converters,
        productCode.put("Electronics" , "854231");//logic circuits, amplifiers, clock and timing circuits, or other circuits

        productCode.put("clothing" , "392620");
        productCode.put("Clothing" , "392620");//Articles of apparel and clothing accessories (including gloves, mittens and mitts)

        productCode.put("furniture" , "940310"); //Metal furniture of a kind used in offices
        productCode.put("Furniture" , "940310");//


        productCode.put("food" , "160232");//Of fowls of the species Gallus domesticus
        productCode.put("Food" , "160232");//we assume "BIRD" for food , if not , too many exceptions


        productCode.put("books" , "482010");//Registers, account books, note books, order books, receipt books, letter pads, memorandum pads, diaries and similar articles
        productCode.put("Books" , "482010");

        productCode.put("toys" , "950300");// Tricycles, scooters, pedal cars and similar wheeled toys; dolls' carriages;
        productCode.put("Toys" , "950300");//dolls; other toys; reduced-size ("scale") models and similar recreational models, working or not; puzzles of all kinds.

        productCode.put("tools" , "392410");//assuming Tableware and kitchenware
        productCode.put("Tools" , "392410");

        productCode.put("beauty" , "330430");// beauty products which are Manicure or pedicure preparations
        productCode.put("Beauty Products" , "330430");

        productCode.put("sports" , "420321");//Specially designed for use in sports
        productCode.put("Sports Equipment" , "420321");//

        productCode.put("automotive" , "401110");//assuming Of a kind used on motor cars (including station wagons and racing cars)
        productCode.put("Automotive Parts" , "401110");//

        productCode.put("chem" , "382200");//Diagnostic or laboratory reagents on a backing, prepared diagnostic 
        productCode.put("Chemicals" , "382200");//laboratory reagents whether or not on a backing, other than those of heading 30.02 or 30.06; certified reference materials.

        productCode.put("plastic or rubber" , "650691");// Of rubber or of plastics
        productCode.put("Plastic , Rubber" , "650691");//


    }

    String codeForProduct = productCode.get(product);

    return codeForProduct;
  }



    public TariffResponse calculateQuote(TariffRequest request) {
        if (request.getQuantity() <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero");
        }

        Product product = productRepository.findByCode(request.getProduct())
                .orElseThrow(() -> new IllegalArgumentException("Unknown product: " + request.getProduct()));

        TariffApiRequest apiRequest = TariffApiRequest.builder()
                .originCountry(resolveCountryToCountryCode(request.getFromCountry()))
                .destCountry(resolveCountryToCountryCode(request.getToCountry()))
                .hs6(resolveProductToProductCode(product.getHsCode()))
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
