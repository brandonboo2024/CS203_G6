package com.example.tariffkey.model;

import lombok.*;

import java.math.BigDecimal;
import java.util.Map;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class TariffApiResponse {

    private double tariffRate;
    private String[] tariffTypes;
    private Integer year;
    private String nomenclature;
    private String url;
    private int httpStatus;
    private String body;
}
