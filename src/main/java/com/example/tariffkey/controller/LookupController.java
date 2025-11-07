package com.example.tariffkey.controller;

import com.example.tariffkey.model.LookupOption;
import com.example.tariffkey.model.LookupResponse;
import com.example.tariffkey.service.LookupService;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/lookups")
@CrossOrigin(origins = "http://localhost:5173")
public class LookupController {

    private final LookupService lookupService;

    public LookupController(LookupService lookupService) {
        this.lookupService = lookupService;
    }

    @GetMapping
    public LookupResponse getLookups() {
        return lookupService.getReporters();
    }

    @GetMapping("/reporters/{reporterCode}/partners")
    public List<LookupOption> getPartners(@PathVariable String reporterCode) {
        return lookupService.getPartnersForReporter(reporterCode);
    }

    @GetMapping("/reporters/{reporterCode}/partners/{partnerCode}/products")
    public List<LookupOption> getProducts(@PathVariable String reporterCode,
                                          @PathVariable String partnerCode) {
        return lookupService.getProductsForRoute(reporterCode, partnerCode);
    }
}
