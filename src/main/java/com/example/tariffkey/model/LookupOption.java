package com.example.tariffkey.model;

/**
 * Simple DTO used by lookup endpoints so the frontend can display
 * a friendly label while still posting the canonical code back.
 * For product lookups we optionally include the HS code and whether
 * a local base price exists.
 */
public record LookupOption(String code, String label, String hsCode, Boolean priceAvailable) {
    public LookupOption(String code, String label) {
        this(code, label, null, null);
    }
}
