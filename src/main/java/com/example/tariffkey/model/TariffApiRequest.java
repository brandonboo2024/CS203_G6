package com.example.tariffkey.model;

import lombok.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class TariffApiRequest {
  // API relevant data
    @NotBlank
    @Pattern(regexp = "^[A-Z0-9]{2,6}$",
             message = "Origin country code must use 2-6 uppercase letters or digits")
    private String originCountry;

    @NotBlank
    @Pattern(regexp = "^[A-Z0-9]{2,6}$",
             message = "Destination country code must use 2-6 uppercase letters or digits")
    private String destCountry;

    @NotBlank
    @Pattern(regexp = "^[0-9]{6}$", message = "HS6 product code must be exactly 6 digits")
    private String hs6; //(PRODUCT CODE)

    @NotBlank
    @Pattern(regexp = "^(ALL|19[0-9]{2}|20[0-9]{2}|21[0-9]{2})$",
             message = "Year must be a valid 4-digit year (1900-2199) or ALL")
    private String year;

  //price calculation relevant data
    @NotNull
    @Min(1)
    @Max(10000)
    private Integer Quantity;

  // miscellaneous fee relevant data
    private boolean handling;
    private boolean inspection;
    private boolean processing;
    private boolean others;
}
