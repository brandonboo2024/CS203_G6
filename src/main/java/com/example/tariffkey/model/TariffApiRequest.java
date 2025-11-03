package com.example.tariffkey.model;

import lombok.*;

import java.math.BigDecimal;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class TariffApiRequest {
  // API relevant data
    private String originCountry;
    private String destCountry;
    private String hs6; //(PRODUCT CODE) //TODO: MATCH STRINGS TO RESPECTIVE PRODUCT CODES
    private String year;

  //price calculation relevant data
    private Integer Quantity;

  // miscellaneous fee relevant data
    private boolean handling;
    private boolean inspection;
    private boolean processing;
    private boolean others;
}
