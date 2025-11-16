package com.example.tariffkey.util;

import org.junit.jupiter.api.Test;

import java.util.Locale;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class IsoCountryLookupTest {

    @Test
    void testNullIso3ReturnsEmpty() {
        assertTrue(IsoCountryLookup.displayNameForIso3(null).isEmpty());
    }

    @Test
    void testBlankIso3ReturnsEmpty() {
        assertTrue(IsoCountryLookup.displayNameForIso3("   ").isEmpty());
    }

    @Test
    void testValidIso3ReturnsName() {
        // Singapore (SG -> SGP) always exists in ISO-3166
        Optional<String> result = IsoCountryLookup.displayNameForIso3("SGP");
        assertTrue(result.isPresent());
        assertEquals("Singapore", result.get());
    }

    @Test
    void testValidIso3CaseInsensitive() {
        Optional<String> result = IsoCountryLookup.displayNameForIso3("sGp");
        assertTrue(result.isPresent());
        assertEquals("Singapore", result.get());
    }

    @Test
    void testInvalidIso3ReturnsEmpty() {
        Optional<String> result = IsoCountryLookup.displayNameForIso3("XYZ"); // unlikely country code
        assertTrue(result.isEmpty());
    }
}
