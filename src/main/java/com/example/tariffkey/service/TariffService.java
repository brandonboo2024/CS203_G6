package com.example.tariffkey.service;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import com.example.tariffkey.model.TariffRequest;
import com.example.tariffkey.model.TariffResponse;

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
