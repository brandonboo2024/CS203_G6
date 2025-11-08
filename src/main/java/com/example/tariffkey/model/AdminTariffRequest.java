package com.example.tariffkey.model;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;

@Data
public class AdminTariffRequest {

    @NotBlank
    @Pattern(regexp = "^[A-Za-z0-9_\\-]{2,64}$", message = "Product code can contain letters, numbers, underscores or hyphens")
    private String product;

    @NotBlank
    @Pattern(regexp = "^[A-Z0-9]{2,6}$", message = "Origin country must be 2-6 uppercase alphanumerics")
    private String originCountry;

    @NotBlank
    @Pattern(regexp = "^[A-Z0-9]{2,6}$", message = "Destination country must be 2-6 uppercase alphanumerics")
    private String destinationCountry;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = true, message = "Rate must be at least 0")
    @DecimalMax(value = "1.0", inclusive = true, message = "Rate must not exceed 1 (decimal form)")
    private Double rate;

    @NotNull
    private LocalDate validFrom;

    @NotNull
    private LocalDate validTo;

    @NotBlank
    @Size(max = 255, message = "Label must be 255 characters or fewer")
    private String label;

    @Size(max = 2000, message = "Notes must be 2000 characters or fewer")
    private String notes;

    private boolean allowOverride;
}
