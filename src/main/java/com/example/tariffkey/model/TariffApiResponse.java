package com.example.tariffkey.model;

import lombok.*;

import java.math.BigDecimal;
import java.util.Map;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class TariffApiResponse {

    private String url;
    private int httpStatus;
    private String body;
    private double tariffRate;
    private String[] tariffTypes;
    private Integer year;
    private String nomenclature;
    private boolean fromCache; // check if from cache or not
}
