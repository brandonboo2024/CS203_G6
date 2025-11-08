package com.example.tariffkey.model;

import lombok.*;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TariffApiResponse {

    private String url;
    private int httpStatus;
    private String body;
    private double tariffRate;
    private String[] tariffTypes;
    private Integer year;
    private String nomenclature;
    private boolean fromCache; // check if from cache or not

    // Admin-managed tariff metadata
    private Long adminTariffId;
    private String label;
    private String sourceLabel;
    private String notes;
    private LocalDate validFrom;
    private LocalDate validTo;
}
