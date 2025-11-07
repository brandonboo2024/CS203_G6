package com.example.tariffkey.model;

/**
 * Simple DTO used by lookup endpoints so the frontend can display
 * a friendly label while still posting the canonical code back.
 */
public record LookupOption(String code, String label) {}
