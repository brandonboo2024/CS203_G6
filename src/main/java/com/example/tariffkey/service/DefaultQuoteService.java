package com.example.tariffkey.service;

import com.example.tariffkey.model.TariffApiRequest;
import com.example.tariffkey.model.TariffApiResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.stereotype.Service;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;

@Service
public class DefaultQuoteService {

  private static final ObjectMapper OM = new ObjectMapper();

  private static String enc(String s) {
    return s == null ? "" : URLEncoder.encode(s.trim(), StandardCharsets.UTF_8);
  }

  private static String safe(String s) {
    return s == null ? "" : s.replace("\"", "'");
  }

  public TariffApiResponse fetchFromApi(TariffApiRequest request) {
    // NOTE: In WITS TRN, reporter = IMPORTER (destination), partner = ORIGIN.
    // Your code currently sets reporter=origin & partner=dest; flip if needed.
    String reporter = enc(request.getOriginCountry());   // e.g., "840"  (importer)
    String partner  = enc(request.getDestCountry()); // e.g., "702"  (exporter) or "000" for World
    String product  = enc(request.getHs6());           // HS-6 e.g., "847130"
    String year     = enc(request.getYear());          // "2022" or "ALL"

    String url = "https://wits.worldbank.org/API/V1/SDMX/V21/datasource/TRN/"
        + "reporter/" + reporter
        + "/partner/" + partner
        + "/product/" + product
        + "/year/" + year
        + "/datatype/reported"
        + "?format=JSON";

    System.out.println(url);

    try {
      HttpClient client = HttpClient.newBuilder()
          .connectTimeout(Duration.ofSeconds(20))
          .build();

      HttpRequest req = HttpRequest.newBuilder()
          .uri(URI.create(url))
          .timeout(Duration.ofSeconds(60))
          .header("Accept", "application/json")
          .GET()
          .build();

      HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());

      // Parse SDMX-JSON into fields
      Parsed p = parseSdmx(resp.body());

      // Build your response DTO
      TariffApiResponse out = TariffApiResponse.builder()
          .url(url)
          .httpStatus(resp.statusCode())
          .body(resp.body())                 // keep raw for debugging
          .tariffRate(p.rateDecimal)         // decimal, e.g., 0.264 for 26.4%
          .tariffTypes(p.tariffTypes)        // e.g., ["MFN"] or ["PREF"]
          .year(p.year)                      // e.g., 2000
          .nomenclature(p.nomenCode)         // e.g., "H1" (HS1996), "H5" (HS2012), etc.
          .build();
      System.out.println("Received back call");

      return out;

    } catch (Exception e) {
      TariffApiResponse out = new TariffApiResponse();
      out.setUrl(url);
      out.setHttpStatus(500);
      out.setBody("{\"error\":\"" + safe(e.getMessage()) + "\"}");
      return out;
    }
  }

  /** ---- Minimal SDMX parser for the fields you care about ---- */
  private static Parsed parseSdmx(String json) throws Exception {
    JsonNode root = OM.readTree(json);

    // 1) Grab the first series (you asked for one product/year, so there should be 1)
    JsonNode seriesNode = root.path("dataSets").path(0).path("series");
    if (seriesNode.isMissingNode() || !seriesNode.fieldNames().hasNext()) {
      return Parsed.empty(); // no data
    }
    String firstSeriesKey = seriesNode.fieldNames().next();
    JsonNode observations = seriesNode.path(firstSeriesKey).path("observations");
    if (observations.isMissingNode() || !observations.fieldNames().hasNext()) {
      return Parsed.empty();
    }

    // 2) Map observation index -> year (from structure.dimensions.observation[0].values)
    JsonNode timeVals = root.path("structure").path("dimensions").path("observation").get(0).path("values");
    // pick the "best" observation (if there are multiple years, choose the latest by year id)
    String bestObsIdx = null;
    int bestYear = Integer.MIN_VALUE;
    Iterator<String> it = observations.fieldNames();
    while (it.hasNext()) {
      String idx = it.next();                   // e.g., "0", "1", ...
      int year = parseIntSafe(timeVals.path(Integer.parseInt(idx)).path("id").asText());
      if (year > bestYear) {
        bestYear = year;
        bestObsIdx = idx;
      }
    }
    if (bestObsIdx == null) return Parsed.empty();

    // 3) Pull the SimpleAverage percent from the chosen observation, convert to decimal
    double simpleAveragePercent = observations.path(bestObsIdx).path(0).asDouble(Double.NaN);
    double rateDecimal = Double.isNaN(simpleAveragePercent) ? 0.0 : simpleAveragePercent / 100.0;

    // 4) Observation attributes: TariffType(s), NomenCode
    // structure.attributes.observation holds code lists; in your sample, each has a single value.
    JsonNode obsAttrs = root.path("structure").path("attributes").path("observation");
    String nomenCode = valueByIdSingle(obsAttrs, "NOMENCODE");  // e.g., "H1"
    // TARIFFTYPE may be MFN or PREF (sometimes one value). Return as array to match your DTO.
    String tariffType = valueByIdSingle(obsAttrs, "TARIFFTYPE"); // e.g., "MFN"
    String[] tariffTypes = (tariffType == null || tariffType.isBlank())
        ? new String[0]
        : new String[]{tariffType};

    return new Parsed(rateDecimal, tariffTypes, bestYear, nomenCode);
  }

  private static int parseIntSafe(String s) {
    try { return Integer.parseInt(s); } catch (Exception e) { return Integer.MIN_VALUE; }
  }

  // returns the first value.id for the attribute with matching id, or null
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

  /** tiny holder for parsed pieces */
  private record Parsed(double rateDecimal, String[] tariffTypes, Integer year, String nomenCode) {
    static Parsed empty() { return new Parsed(0.0, new String[0], null, null); }
  }

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

    }

}
