// package com.example.tariffkey.service;

// import com.example.tariffkey.model.TariffRequest;
// import com.example.tariffkey.model.TariffResponse;
// import org.springframework.stereotype.Service;

// @Service
// public class TariffService {

//     public TariffResponse calculate(TariffRequest request) {
//         double tariffRate = 0.0;

//         // double tariffAmount = request.getItemPrice() * request.getTariffRate();
//         // double total = request.getItemPrice() + tariffAmount;

//         // TariffResponse response = new TariffResponse();
//         // response.setItemPrice(request.getItemPrice());
//         // response.setTariffRate(request.getTariffRate());
//         // response.setTotalPrice(total);
//         // return response;

//     }

    
// }
package com.example.tariffkey.service;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.example.tariffkey.model.TariffRequest;
import com.example.tariffkey.model.TariffResponse;

@Service
public class TariffService {

    public TariffResponse calculate(TariffRequest request) {
        String from = request.getFromCountry(); // e.g., "SGP"
        String to = request.getToCountry();     // e.g., "USA"
        String product = request.getProduct();  // e.g., "847130"
        String year = "2022";                   // or make this dynamic

        // Build WITS API URL
        String url = String.format(
            "https://wits.worldbank.org/API/V1/SDMX/V21/COMTRADE/TARIFF/REPORTER/%s/PARTNER/%s/PRODUCT/%s/YEAR/%s",
            from, to, product, year
        );

        RestTemplate restTemplate = new RestTemplate();
        String response = restTemplate.getForObject(url, String.class);
        // Parse the JSON to extract the tariff rate
        double tariffRate = 0.0;
        try {
            JSONObject json = new JSONObject(response);
            JSONArray data = json.getJSONArray("data");
            if (data.length() > 0) {
                JSONObject first = data.getJSONObject(0);
                tariffRate = first.getDouble("TariffValue") / 100.0; // Convert percent to decimal
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Use a default item price (e.g., 100) since WITS does not provide prices
        double itemPrice = 100.0;
        double tariffAmount = itemPrice * tariffRate;
        double total = itemPrice + tariffAmount;

        TariffResponse tariffResponse = new TariffResponse();
        tariffResponse.setItemPrice(itemPrice);
        tariffResponse.setTariffRate(tariffRate);
        tariffResponse.setTotalPrice(total);
        return tariffResponse;
    }
}