package com.example.tariffkey.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.lang.Integer;
import java.util.stream.*;

/**
 * Minimal request payload for a tariff quote between two countries for one product.
 * Keep it simple; your service can map this to whatever API you use (WITS, national HTS/TARIC, etc.)
 */
public record TariffApiRequest(
        String importerIso2,              // destination country (ISO-3166 alpha-2, e.g. "SG")
        String exporterIso2,              // origin country (ISO-3166 alpha-2, e.g. "CN")
        String hsCode,                    // HS code (chapter/4-digit/6-digit/or full line; we'll sanitize to HS6)
        BigDecimal quantity,              // e.g. 100.0
        String unit,                      // e.g. "kg", "no.", "l"
        LocalDate startDate,              // inclusive (used to derive years for the API)
        LocalDate endDate,                // inclusive
        BigDecimal customsValue,          // optional: CIF value in 'currency' for ad valorem (can be null)
        String currency,                  // ISO-4217 (e.g. "USD", "SGD")
        boolean usePreferentialIfAvailable // try preferential/FTA rate when available; otherwise MFN
) {

    public TariffApiRequest {
        // --- requireds ---
        require(importerIso2, "importerIso2");
        require(exporterIso2, "exporterIso2");
        require(hsCode, "hsCode");
        require(quantity, "quantity");
        require(unit, "unit");
        require(startDate, "startDate");
        require(endDate, "endDate");
        require(currency, "currency");

        if (quantity.signum() <= 0) throw new IllegalArgumentException("quantity must be > 0");
        if (endDate.isBefore(startDate)) throw new IllegalArgumentException("endDate must be on/after startDate");
    }

    private static <T> void require(T v, String name) {
        Objects.requireNonNull(v, name + " is required");
    }

    /** Digits-only HS code, truncated to HS6 if longer (safe for HS6-level APIs). */
    public String hs6() {
        String digits = hsCode.replaceAll("\\D", "");
        return digits.substring(0, Math.min(6, digits.length()));
    }

    /** Unique list of calendar years covered by the [startDate, endDate] (handy for year-based APIs). */
    public List<Integer> years() {
        return startDate
                .datesUntil(endDate.plusDays(1))
                .map(LocalDate::getYear)
                // .boxed()
                .distinct()
                .toList();
    }

    /** Upper-cased ISO-2 importer (some APIs are case sensitive). */
    public String importerIso2Upper() { return importerIso2.toUpperCase(); }

    /** Upper-cased ISO-2 exporter (some APIs are case sensitive). */
    public String exporterIso2Upper() { return exporterIso2.toUpperCase(); }

    /** True if a customs value was provided (ad valorem duties can be computed directly). */
    public boolean hasCustomsValue() { return customsValue != null && customsValue.signum() >= 0; }
}
