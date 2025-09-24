package com.example.tariffkey.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.tariffkey.model.TariffRequest;
import com.example.tariffkey.model.TariffResponse;
// import com.example.tariffkey.service.TariffService;
import com.example.tariffkey.service.TariffService2;
import org.springframework.web.bind.annotation.CrossOrigin;

@RestController
@RequestMapping("/api/tariff")
@CrossOrigin(origins = "http://localhost:5173")
public class TariffController {

    private final TariffService2 tariffService2;

    public TariffController(TariffService2 tariffService2) {
        this.tariffService2 = tariffService2;
    }

    @PostMapping("/calculate")
    public TariffResponse calculate(@RequestBody TariffRequest request) {
        System.out.println("Received response");
        return tariffService2.calculate(request);
    }
}
