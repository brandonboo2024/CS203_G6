package com.example.tariffkey.service;

import com.example.tariffkey.model.LookupOption;
import com.example.tariffkey.model.LookupResponse;
import com.example.tariffkey.model.Product;
import com.example.tariffkey.repository.ProductRepository;
import com.example.tariffkey.repository.WitsTariffRepository;
import com.example.tariffkey.util.IsoCountryLookup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger log = LoggerFactory.getLogger(LookupService.class);

    private final WitsTariffRepository witsTariffRepository;
    private final ProductRepository productRepository;
    private final Map<String, String> reporterLabelCache = new ConcurrentHashMap<>();

    public LookupService(WitsTariffRepository witsTariffRepository,
                         ProductRepository productRepository) {
        this.witsTariffRepository = witsTariffRepository;
        this.productRepository = productRepository;
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
        List<String> hsCodes = witsTariffRepository.findProductsByRoute(reporterCode, partnerCode);
        if (hsCodes.isEmpty()) {
            throw new IllegalArgumentException("No products available for the selected countries");
        }
        Map<String, Product> pricedProducts = productRepository.findByHsCodeIn(hsCodes).stream()
                .collect(Collectors.toMap(Product::getHsCode, product -> product, (left, right) -> left));

        List<LookupOption> options = hsCodes.stream()
                .map(hs -> {
                    Product product = pricedProducts.get(hs);
                    if (product == null) {
                        String label = "HS " + hs + " (price required)";
                        return new LookupOption(hs, label, hs, Boolean.FALSE);
                    }
                    return new LookupOption(product.getCode(), formatProductLabel(product), product.getHsCode(), Boolean.TRUE);
                })
                .collect(Collectors.toList());
        return options;
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

    private static String formatProductLabel(Product product) {
        String friendly = product.getCode()
                .replace('_', ' ')
                .replace('-', ' ')
                .trim();
        if (!friendly.isEmpty()) {
            friendly = Character.toUpperCase(friendly.charAt(0)) + friendly.substring(1);
        }
        return friendly + " (HS " + product.getHsCode() + ")";
    }
}
