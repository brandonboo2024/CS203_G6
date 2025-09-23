package com.example.tariffkey.service;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import com.example.tariffkey.model.TariffRequest;
import com.example.tariffkey.model.TariffResponse;

// import org.json.JSONArray;
// import org.json.JSONObject;
// import org.springframework.stereotype.Service;
// import org.springframework.web.client.RestTemplate;
//
// import com.example.tariffkey.model.TariffRequest;
// import com.example.tariffkey.model.TariffResponse;

// @Service
// public class TariffService {
//
//     public TariffResponse calculate(TariffRequest request) {
//         String from = request.getFromCountry(); // e.g., "SGP"
//         String to = request.getToCountry();     // e.g., "USA"
//         String product = request.getProduct();  // e.g., "847130"
//         String year = "2022";                   // or make this dynamic
//
//         // Build WITS API URL
//         String url = String.format(
//             "https://wits.worldbank.org/API/V1/SDMX/V21/COMTRADE/TARIFF/REPORTER/%s/PARTNER/%s/PRODUCT/%s/YEAR/%s",
//             from, to, product, year
//         );
//
//         RestTemplate restTemplate = new RestTemplate();
//         String response = restTemplate.getForObject(url, String.class);
//         // Parse the JSON to extract the tariff rate
//         double tariffRate = 0.0;
//         try {
//             JSONObject json = new JSONObject(response);
//             JSONArray data = json.getJSONArray("data");
//             if (data.length() > 0) {
//                 JSONObject first = data.getJSONObject(0);
//                 tariffRate = first.getDouble("TariffValue") / 100.0; // Convert percent to decimal
//             }
//         } catch (Exception e) {
//             e.printStackTrace();
//         }
//
//         // Use a default item price (e.g., 100) since WITS does not provide prices
//         double itemPrice = 100.0;
//         double tariffAmount = itemPrice * tariffRate;
//         double total = itemPrice + tariffAmount;
//
//         TariffResponse tariffResponse = new TariffResponse();
//         tariffResponse.setItemPrice(itemPrice);
//         tariffResponse.setTariffRate(tariffRate);
//         tariffResponse.setTotalPrice(total);
//         return tariffResponse;
//     }
// }
@Service
public class TariffService {

    private final JdbcTemplate jdbc;

    public TariffService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public TariffResponse calculate(TariffRequest req) {
        // get base price
        Double basePrice = jdbc.queryForObject(
            "SELECT base_price FROM product WHERE code = ?",
            Double.class, req.getProduct());

        // get tariff rate (prefer override)
        Double tariffRate = jdbc.queryForObject("""
            SELECT COALESCE(
              (SELECT rate_percent FROM route_tariff_override 
                 WHERE product_code = ? AND origin_country = ? AND dest_country = ?),
              (SELECT rate_percent FROM product_tariff_default WHERE product_code = ?)
            )
            """,
            Double.class,
            req.getProduct(), req.getFromCountry(), req.getToCountry(), req.getProduct()
        );

        double itemPrice = req.getQuantity() * basePrice;
        double tariffAmount = itemPrice * (tariffRate / 100.0);

        double handlingFee = req.isHandling() ? getFee("handling") : 0;
        double inspectionFee = req.isInspection() ? getFee("inspection") : 0;
        double processingFee = req.isProcessing() ? getFee("processing") : 0;
        double otherFees = req.isOthers() ? getFee("others") : 0;

        double total = itemPrice + tariffAmount + handlingFee + inspectionFee + processingFee + otherFees;

        TariffResponse resp = new TariffResponse();
        resp.setItemPrice(itemPrice);
        resp.setTariffRate(tariffRate);
        resp.setTariffAmount(tariffAmount);
        resp.setHandlingFee(handlingFee);
        resp.setInspectionFee(inspectionFee);
        resp.setProcessingFee(processingFee);
        resp.setOtherFees(otherFees);
        resp.setTotalPrice(total);

        return resp;
    }

    private double getFee(String fee) {
        return jdbc.queryForObject("SELECT amount FROM fee_schedule WHERE fee = CAST(? AS fee_type)", Double.class, fee);
    }
}
