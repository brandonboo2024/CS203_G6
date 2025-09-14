package com.example.tariffkey.controller;

import com.example.tariffkey.model.TariffRequest;
import com.example.tariffkey.model.TariffResponse;
import com.example.tariffkey.service.TariffService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tariff")
public class TariffController {

    private final TariffService tariffService;

    public TariffController(TariffService tariffService) {
        this.tariffService = tariffService;
    }

    @PostMapping("/calculate")
    public TariffResponse calculate(@RequestBody TariffRequest request) {
        return tariffService.calculate(request);
    }
}
