package com.example.tariffkey.service;

import com.example.tariffkey.model.LookupOption;
import com.example.tariffkey.model.LookupResponse;
import com.example.tariffkey.model.Product;
import com.example.tariffkey.model.WitsCountryMetadata;
import com.example.tariffkey.model.WitsProductMetadata;
import com.example.tariffkey.model.WitsProductMetadataId;
import com.example.tariffkey.repository.ProductRepository;
import com.example.tariffkey.repository.WitsCountryMetadataRepository;
import com.example.tariffkey.repository.WitsProductMetadataRepository;
import com.example.tariffkey.repository.WitsTariffRepository;
import com.example.tariffkey.util.IsoCountryLookup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class LookupService {

    private static final Logger log = LoggerFactory.getLogger(LookupService.class);

    private final WitsTariffRepository witsTariffRepository;
    private final WitsCountryMetadataRepository countryMetadataRepository;
    private final WitsProductMetadataRepository productMetadataRepository;
    private final ProductRepository productRepository;
    private final Map<String, String> countryLabelCache = new ConcurrentHashMap<>();
    private final Map<String, String> productLabelCache = new ConcurrentHashMap<>();

    public LookupService(WitsTariffRepository witsTariffRepository,
                         WitsCountryMetadataRepository countryMetadataRepository,
                         WitsProductMetadataRepository productMetadataRepository,
                         ProductRepository productRepository) {
        this.witsTariffRepository = witsTariffRepository;
        this.countryMetadataRepository = countryMetadataRepository;
        this.productMetadataRepository = productMetadataRepository;
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
                .map(code -> new LookupOption(code, countryLabel(code)))
                .collect(Collectors.toList());
    }

    public List<LookupOption> getProductsForRoute(String reporterCode, String partnerCode) {
        ensureReporterExists(reporterCode);
        if (!StringUtils.hasText(partnerCode)) {
            throw new IllegalArgumentException("Partner code is required");
        }
        List<WitsTariffRepository.ProductSample> samples = witsTariffRepository.findProductSamplesByRoute(
                reporterCode, partnerCode);
        if (samples.isEmpty()) {
            throw new IllegalArgumentException("No products available for the selected countries");
        }
        List<String> hsCodes = samples.stream()
                .map(WitsTariffRepository.ProductSample::getProductCode)
                .toList();

        Map<String, Product> pricedProducts = productRepository.findByHsCodeIn(hsCodes).stream()
                .collect(Collectors.toMap(Product::getHsCode, product -> product, (left, right) -> left));

        Map<String, LookupOption> deduped = new LinkedHashMap<>();
        for (WitsTariffRepository.ProductSample sample : samples) {
            String hsCode = sample.getProductCode();
            Product product = pricedProducts.get(hsCode);
            boolean priceAvailable = product != null;
            String baseLabel = productLabel(sample.getNomenCode(), hsCode);
            String label = priceAvailable ? baseLabel : baseLabel + " (price required)";
            String optionCode = priceAvailable ? product.getCode() : hsCode;
            String optionHsCode = priceAvailable ? product.getHsCode() : hsCode;
            deduped.putIfAbsent(optionCode, new LookupOption(optionCode, label, optionHsCode, priceAvailable));
        }
        return new ArrayList<>(deduped.values());
    }

    private LookupOption toReporterOption(String code, String sourceFile) {
        String label = countryMetadataRepository.findById(code)
                .map(this::formatCountryLabel)
                .orElseGet(() -> fallbackReporterLabel(code, sourceFile));
        countryLabelCache.putIfAbsent(code, label);
        return new LookupOption(code, label);
    }

    private String countryLabel(String code) {
        return countryLabelCache.computeIfAbsent(code, key ->
                countryMetadataRepository.findById(key)
                        .map(this::formatCountryLabel)
                        .orElse(key));
    }

    private String formatCountryLabel(WitsCountryMetadata metadata) {
        String name = StringUtils.hasText(metadata.getCountryName())
                ? metadata.getCountryName()
                : metadata.getCountryCode();
        if (!StringUtils.hasText(name)) {
            name = metadata.getIso3();
        }
        if (!StringUtils.hasText(name)) {
            name = metadata.getCountryCode();
        }
        List<String> markers = new ArrayList<>();
        if (StringUtils.hasText(metadata.getIso3())) {
            markers.add(metadata.getIso3().toUpperCase(Locale.ROOT));
        }
        if (StringUtils.hasText(metadata.getCountryCode())
                && (markers.isEmpty()
                || !metadata.getCountryCode().equalsIgnoreCase(markers.get(markers.size() - 1)))) {
            markers.add(metadata.getCountryCode());
        }
        return markers.isEmpty() ? name : name + " (" + String.join(" · ", markers) + ")";
    }

    private String fallbackReporterLabel(String code, String sourceFile) {
        String iso3 = parseIso3FromSource(sourceFile);
        return IsoCountryLookup.displayNameForIso3(iso3)
                .map(name -> {
                    List<String> markers = new ArrayList<>();
                    if (StringUtils.hasText(iso3)) {
                        markers.add(iso3);
                    }
                    if (StringUtils.hasText(code)
                            && (markers.isEmpty() || !code.equalsIgnoreCase(markers.get(markers.size() - 1)))) {
                        markers.add(code);
                    }
                    return markers.isEmpty() ? name : name + " (" + String.join(" · ", markers) + ")";
                })
                .orElse(code);
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

    private String productLabel(String nomenCode, String productCode) {
        String cacheKey = nomenCode + ":" + productCode;
        return productLabelCache.computeIfAbsent(cacheKey, key -> resolveProductLabel(nomenCode, productCode));
    }

    private String resolveProductLabel(String nomenCode, String productCode) {
        Optional<WitsProductMetadata> precise = productMetadataRepository.findById(
                new WitsProductMetadataId(nomenCode, productCode));
        if (precise.isPresent()) {
            return formatProductDescription(precise.get().getDescription(), productCode);
        }
        Optional<WitsProductMetadata> hsLevel = productMetadataRepository.findById(
                new WitsProductMetadataId("HS", productCode));
        if (hsLevel.isPresent()) {
            return formatProductDescription(hsLevel.get().getDescription(), productCode);
        }
        return productMetadataRepository.findFirstByIdProductCode(productCode)
                .map(meta -> formatProductDescription(meta.getDescription(), productCode))
                .orElse("HS " + productCode);
    }

    private String formatProductDescription(String description, String productCode) {
        String base = StringUtils.hasText(description) ? description : "HS " + productCode;
        return base + " (HS " + productCode + ")";
    }
}
