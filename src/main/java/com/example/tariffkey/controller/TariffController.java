package com.example.tariffkey.controller;

import com.example.tariffkey.entity.*;
import com.example.tariffkey.service.TariffService;
import org.springframework.web.bind.annotation.*;
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
            @RequestParam double basePrice,
            @RequestParam(required = false) List<String> fees
    ) {
        double tariffAmount = tariffService.calculateTariff(productCode, originCountry, destCountry, basePrice);
        double feeTotal = tariffService.calculateFees(fees);

        double finalCost = basePrice + tariffAmount + feeTotal;

        return Map.of(
                "productCode", productCode,
                "originCountry", originCountry,
                "destCountry", destCountry,
                "basePrice", basePrice,
                "tariffAmount", tariffAmount,
                "feeTotal", feeTotal,
                "finalCost", finalCost
        );
    }

    @GetMapping("/fees")
    public List<FeeSchedule> getFees() {
        return tariffService.getAllFees();
    }
}
