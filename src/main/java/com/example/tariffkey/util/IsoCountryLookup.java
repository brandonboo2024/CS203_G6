package com.example.tariffkey.util;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Optional;

/**
 * Small helper that exposes ISO-3166 country names without adding
 * a third-party dependency. Maps ISO3 codes to their English names.
 */
public final class IsoCountryLookup {

    private static final Map<String, String> ISO3_TO_NAME = buildIso3Map();

    private IsoCountryLookup() {}

    public static Optional<String> displayNameForIso3(String iso3) {
        if (iso3 == null || iso3.isBlank()) {
            return Optional.empty();
        }
        return Optional.ofNullable(ISO3_TO_NAME.get(iso3.toUpperCase(Locale.ROOT)));
    }

    private static Map<String, String> buildIso3Map() {
        Map<String, String> map = new HashMap<>();
        for (String iso2 : Locale.getISOCountries()) {
            Locale locale = new Locale("", iso2);
            try {
                String iso3 = locale.getISO3Country().toUpperCase(Locale.ROOT);
                map.put(iso3, locale.getDisplayCountry(Locale.ENGLISH));
            } catch (MissingResourceException ignored) {
                // Skip entries that do not expose ISO3 names
            }
        }
        return Map.copyOf(map);
    }
}
