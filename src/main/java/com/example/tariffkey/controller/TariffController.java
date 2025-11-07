package com.example.tariffkey.controller;

import com.example.tariffkey.model.*;
import com.example.tariffkey.service.DefaultQuoteService;
import com.example.tariffkey.service.TariffManagementService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Validated
@RestController
@RequestMapping("/api/tariff")
@CrossOrigin(origins = "http://localhost:5173")
public class TariffController {

    private final DefaultQuoteService quoteService;
    private final TariffManagementService tariffManagementService;

    public TariffController(DefaultQuoteService quoteService,
                            TariffManagementService tariffManagementService) {
        this.quoteService = quoteService;
        this.tariffManagementService = tariffManagementService;
    }

    @PostMapping("/calculate")
    public TariffResponse calculate(@RequestBody TariffRequest request) {
        return quoteService.calculateQuote(request);
    }

    @PostMapping("/quote")
    public TariffApiResponse quote(@RequestBody TariffApiRequest quote) {
        return quoteService.fetchQuote(quote);
    }

    @GetMapping("/all")
    public List<Tariff> getAllTariffs() {
        return tariffManagementService.getAllTariffs();
    }

    @PostMapping("/add")
    public Tariff addTariff(@RequestBody Tariff tariff) {
        return tariffManagementService.addTariff(tariff);
    }

    @DeleteMapping("/{id}")
    public void deleteTariff(@PathVariable long id) {
        tariffManagementService.deleteTariff(id);
    }

    @GetMapping
    public String testEndpoint() {
        return "TariffController active";
    }
}
