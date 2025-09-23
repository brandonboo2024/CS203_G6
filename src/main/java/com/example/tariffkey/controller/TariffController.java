package com.example.tariffkey.controller;

import com.example.tariffkey.entity.FeeSchedule;
import com.example.tariffkey.service.TariffService;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tariff")
public class TariffController {

    private final TariffService tariffService;

    public TariffController(TariffService tariffService) {
        this.tariffService = tariffService;
    }

    @GetMapping("/calculate")
    public Map<String, Object> calculate(
            @RequestParam String productCode,
            @RequestParam String originCountry,
            @RequestParam String destCountry,
            @RequestParam BigDecimal basePrice
    ) {
        BigDecimal tariff = tariffService.calculateTariff(productCode, originCountry, destCountry, basePrice);
        return Map.of(
                "productCode", productCode,
                "originCountry", originCountry,
                "destCountry", destCountry,
                "basePrice", basePrice,
                "tariff", tariff
        );
    }

    @GetMapping("/fees")
    public List<FeeSchedule> getFees() {
        return tariffService.getAllFees();
    }
}
