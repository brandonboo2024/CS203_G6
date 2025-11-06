package com.example.tariffkey.service;

import com.example.tariffkey.model.*;
import com.example.tariffkey.repository.FeeScheduleRepository;
import com.example.tariffkey.repository.ProductRepository;
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

@Service
public class DefaultQuoteService {

    private static final ObjectMapper OM = new ObjectMapper();

    private final ProductRepository productRepository;
    private final FeeScheduleRepository feeScheduleRepository;
    private final HttpClient httpClient;
    private final String apiBaseUrl;

    @org.springframework.beans.factory.annotation.Autowired
    public DefaultQuoteService(
            ProductRepository productRepository,
            FeeScheduleRepository feeScheduleRepository,
            @Value("${wits.api.base-url:https://wits.worldbank.org/API/V1/SDMX/V21/datasource/TRN}") String apiBaseUrl
    ) {
        this(productRepository, feeScheduleRepository,
                HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(20)).build(),
                apiBaseUrl);
    }

    DefaultQuoteService(ProductRepository productRepository,
                        FeeScheduleRepository feeScheduleRepository,
                        HttpClient httpClient,
                        String apiBaseUrl) {
        this.productRepository = productRepository;
        this.feeScheduleRepository = feeScheduleRepository;
        this.httpClient = httpClient;
        this.apiBaseUrl = apiBaseUrl.endsWith("/") ? apiBaseUrl.substring(0, apiBaseUrl.length() - 1) : apiBaseUrl;
    }

    public TariffApiResponse fetchQuote(TariffApiRequest request) {
        String reporter = enc(request.getOriginCountry());
        String partner = enc(request.getDestCountry());
        String product = enc(request.getHs6());
        String year = enc(request.getYear());

        String url = apiBaseUrl
                + "/reporter/" + reporter
                + "/partner/" + partner
                + "/product/" + product
                + "/year/" + year
                + "/datatype/reported"
                + "?format=JSON";

        try {
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(60))
                    .header("Accept", "application/json")
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            Parsed parsed = parseSdmx(response.body());

            return TariffApiResponse.builder()
                    .url(url)
                    .httpStatus(response.statusCode())
                    .body(response.body())
                    .tariffRate(parsed.rateDecimal)
                    .tariffTypes(parsed.tariffTypes)
                    .year(parsed.year)
                    .nomenclature(parsed.nomenCode)
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
        productCode.put("electronics" , "84-85_MachElec");//Machine and Electronic
        productCode.put("Electronics" , "84-85_MachElec");//

        productCode.put("clothing" , "50-63_TextCloth");
        productCode.put("Clothing" , "50-63_TextCloth");//Textiles and Clothing

        productCode.put("furniture" , "UNCTAD-SoP3"); //Consumer goods
        productCode.put("Furniture" , "UNCTAD-SoP3");//


        productCode.put("food" , "16-24_FoodProd");//Food Products
        productCode.put("Food" , "16-24_FoodProd");//


        productCode.put("books" , "Textiles");//no books so put textiles
        productCode.put("Books" , "Textiles");

        productCode.put("toys" , "Total");//no toys so assume is under "Total , all products category"
        productCode.put("Toys" , "Total");

        productCode.put("tools" , "manuf");//assuming the tools are used for manufacturing , it should fall under here
        productCode.put("Tools" , "manuf");

        productCode.put("beauty" , "UNCTAD-SoP3");// beauty products should fall under consumer goods category
        productCode.put("Beauty Products" , "UNCTAD-SoP3");

        productCode.put("sports" , "64-67_Footwear");//i think the closest is footwear caetgory, unless we want consumer goods, but that 
        productCode.put("Sports Equipment" , "64-67_Footwear");//shouldnt be used too much

        productCode.put("automotive" , "Transp");//machinery and transport
        productCode.put("Automotive Parts" , "Transp");//

        productCode.put("chem" , "28-38_Chemicals");//Chemicals
        productCode.put("Chemicals" , "28-38_Chemicals");//

        productCode.put("plastic or rubber" , "39-40_PlastiRub");//For materials of Plastic and Rubber
        productCode.put("Plastic , Rubber" , "39-40_PlastiRub");//

        productCode.put("misc" , "90-99_Miscellan");//For materials of Plastic and Rubber
        productCode.put("Miscallaneous" , "90-99_Miscellan");//

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
        response.setLabel("Average tariff rate (WITS)");
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

    private record Parsed(double rateDecimal, String[] tariffTypes, Integer year, String nomenCode) {
        static Parsed empty() {
            return new Parsed(0.0, new String[0], null, null);
        }
    }
}
