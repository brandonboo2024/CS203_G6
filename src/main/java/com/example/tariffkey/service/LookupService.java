package com.example.tariffkey.service;

import com.example.tariffkey.model.LookupOption;
import com.example.tariffkey.model.LookupResponse;
import com.example.tariffkey.repository.WitsTariffRepository;
import com.example.tariffkey.util.IsoCountryLookup;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class LookupService {

    private final WitsTariffRepository witsTariffRepository;
    private final Map<String, String> reporterLabelCache = new ConcurrentHashMap<>();

    public LookupService(WitsTariffRepository witsTariffRepository) {
        this.witsTariffRepository = witsTariffRepository;
    }

    public LookupResponse getReporters() {
        List<LookupOption> reporters = witsTariffRepository.findReporterSamples().stream()
                .map(sample -> toReporterOption(sample.getReporterIso(), sample.getSourceFile()))
                .sorted(Comparator.comparing(LookupOption::label))
                .toList();
        return new LookupResponse(reporters);
    }

    public List<LookupOption> getPartnersForReporter(String reporterCode) {
        ensureReporterExists(reporterCode);
        List<String> partnerCodes = witsTariffRepository.findPartnersByReporter(reporterCode);
        if (partnerCodes.isEmpty()) {
            throw new IllegalArgumentException("No partners available for reporter " + reporterCode);
        }
        return partnerCodes.stream()
                .map(code -> new LookupOption(code, reporterLabelCache.getOrDefault(code, code)))
                .collect(Collectors.toList());
    }

    public List<LookupOption> getProductsForRoute(String reporterCode, String partnerCode) {
        ensureReporterExists(reporterCode);
        if (!StringUtils.hasText(partnerCode)) {
            throw new IllegalArgumentException("Partner code is required");
        }
        List<String> products = witsTariffRepository.findProductsByRoute(reporterCode, partnerCode);
        if (products.isEmpty()) {
            throw new IllegalArgumentException("No products available for the selected countries");
        }
        return products.stream()
                .map(code -> new LookupOption(code, formatProductLabel(code)))
                .collect(Collectors.toList());
    }

    private LookupOption toReporterOption(String code, String sourceFile) {
        String iso3 = parseIso3FromSource(sourceFile);
        String label = IsoCountryLookup.displayNameForIso3(iso3)
                .map(name -> name + " (" + code + ")")
                .orElse(code);
        reporterLabelCache.put(code, label);
        return new LookupOption(code, label);
    }

    private static String parseIso3FromSource(String sourceFile) {
        if (!StringUtils.hasText(sourceFile)) {
            return null;
        }
        String trimmed = sourceFile.endsWith(".zip")
                ? sourceFile.substring(0, sourceFile.length() - 4)
                : sourceFile;
        String[] parts = trimmed.split("_");
        if (parts.length >= 4) {
            return parts[2].replaceAll("[^A-Za-z]", "").toUpperCase(Locale.ROOT);
        }
        return null;
    }

    private void ensureReporterExists(String reporterCode) {
        if (!StringUtils.hasText(reporterCode) || !witsTariffRepository.existsByReporterIso(reporterCode)) {
            throw new IllegalArgumentException("Unknown reporter code: " + reporterCode);
        }
    }

    private static String formatProductLabel(String code) {
        return "HS " + code;
    }
}
