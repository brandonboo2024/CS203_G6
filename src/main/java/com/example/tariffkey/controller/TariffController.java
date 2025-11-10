package com.example.tariffkey.controller;

import com.example.tariffkey.model.*;
import com.example.tariffkey.service.DefaultQuoteService;
import com.example.tariffkey.service.TariffHistoryService;
import com.example.tariffkey.service.TariffManagementService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tariff")
@CrossOrigin(origins = {"http://localhost:5173","https://frontend-production-a446.up.railway.app"})
public class TariffController {

    private final DefaultQuoteService quoteService;
    private final TariffManagementService tariffManagementService;
    private final TariffHistoryService tariffHistoryService;

    public TariffController(DefaultQuoteService quoteService,
                            TariffManagementService tariffManagementService,
                            TariffHistoryService tariffHistoryService) {
        this.quoteService = quoteService;
        this.tariffManagementService = tariffManagementService;
        this.tariffHistoryService = tariffHistoryService;
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
    public Tariff addTariff(@Valid @RequestBody AdminTariffRequest tariff,
                            Authentication authentication) {
        String createdBy = authentication != null ? authentication.getName() : null;
        return tariffManagementService.addTariff(tariff, createdBy);
    }

    @DeleteMapping("/{id}")
    public void deleteTariff(@PathVariable long id) {
        tariffManagementService.deleteTariff(id);
    }

    @PostMapping("/history")
    public TariffHistoryResponse getHistory(@Valid @RequestBody TariffHistoryRequest request) {
        return tariffHistoryService.getHistory(request);
    }

    @GetMapping
    public String testEndpoint() {
        return "TariffController active";
    }
}
