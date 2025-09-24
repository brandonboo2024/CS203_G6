package com.example.tariffkey.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.tariffkey.model.TariffRequest;
import com.example.tariffkey.model.TariffResponse;
import com.example.tariffkey.service.TariffService;
import org.springframework.web.bind.annotation.CrossOrigin;

@RestController
@RequestMapping("/api/tariff")
@CrossOrigin(origins = "http://localhost:5173")
public class TariffController {

    private final TariffService tariffService;

    public TariffController(TariffService tariffService) {
        this.tariffService = tariffService;
    }

    @PostMapping("/calculate")
    public TariffResponse calculate(@RequestBody TariffRequest request) {
        System.out.println("Received response");
        return tariffService.calculate(request);
    }
}
