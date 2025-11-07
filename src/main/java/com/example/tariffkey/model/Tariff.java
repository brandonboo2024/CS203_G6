package com.example.tariffkey.model;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;

public class Tariff {
    private int id;

    @NotBlank
    @Pattern(regexp = "^(electronics|clothing|furniture|food|books|toys|tools|beauty|sports|automotive)$",
             message = "Product must be one of the supported categories")
    private String product;

    @NotBlank
    @Pattern(regexp = "^(SG|US|MY|TH|VN|ID|PH|KR|IN|AU|GB|DE|FR|IT|ES|CA|BR|MX|RU|ZA|CN|JP)$",
             message = "Origin country code must be one of the supported countries")
    private String originCountry;

    @NotBlank
    @Pattern(regexp = "^(SG|US|MY|TH|VN|ID|PH|KR|IN|AU|GB|DE|FR|IT|ES|CA|BR|MX|RU|ZA|CN|JP)$",
             message = "Destination country code must be one of the supported countries")
    private String destinationCountry;

    @Positive(message = "Tariff rate must be positive")
    @DecimalMin(value = "0.0", inclusive = false, message = "Tariff rate must be greater than 0")
    @DecimalMax(value = "100.0", message = "Tariff rate cannot exceed 100%")
    private double rate;

    public Tariff() {}

    public Tariff(int id, String product, String originCountry, String destinationCountry, double rate) {
        this.id = id;
        this.product = product;
        this.originCountry = originCountry;
        this.destinationCountry = destinationCountry;
        this.rate = rate;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getProduct() { return product; }
    public void setProduct(String product) { this.product = product; }

    public String getOriginCountry() { return originCountry; }
    public void setOriginCountry(String originCountry) { this.originCountry = originCountry; }

    public String getDestinationCountry() { return destinationCountry; }
    public void setDestinationCountry(String destinationCountry) { this.destinationCountry = destinationCountry; }

    public double getRate() { return rate; }
    public void setRate(double rate) { this.rate = rate; }
}
