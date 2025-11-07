package com.example.tariffkey.model;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import com.example.tariffkey.validation.ValidDateRange;

@ValidDateRange(fromField = "calculationFrom", toField = "calculationTo",
                maxYearsPast = 10, maxYearsFuture = 10,
                message = "Dates must be within 10 years past/future and start must be before end")
public class TariffRequest {
    @NotBlank
    @Pattern(regexp = "^(SG|US|MY|TH|VN|ID|PH|KR|IN|AU|GB|DE|FR|IT|ES|CA|BR|MX|RU|ZA|CN|JP)$",
             message = "Country code must be one of the supported countries")
    private String fromCountry;

    @NotBlank
    @Pattern(regexp = "^(SG|US|MY|TH|VN|ID|PH|KR|IN|AU|GB|DE|FR|IT|ES|CA|BR|MX|RU|ZA|CN|JP)$",
             message = "Country code must be one of the supported countries")
    private String toCountry;

    @NotBlank
    @Pattern(regexp = "^(electronics|clothing|furniture|food|books|toys|tools|beauty|sports|automotive)$",
             message = "Product must be one of the supported categories")
    private String product;

    @NotNull
    @Min(1)
    @Max(10000)
    private Integer quantity;

    private boolean handling;
    private boolean inspection;
    private boolean processing;
    private boolean others;

    // datetime fields (optional) - frontend validates within 10 years past/future
    private String calculationFrom;
    private String calculationTo;

    public String getFromCountry() {
        return fromCountry;
    }
    public void setFromCountry(String fromCountry) {
        this.fromCountry = fromCountry;
    }
    public String getToCountry() {
        return toCountry;
    }
    public void setToCountry(String toCountry) {
        this.toCountry = toCountry;
    }
    public String getProduct() {
        return product;
    }
    public void setProduct(String product) {
        this.product = product;
    }
    public Integer getQuantity() {
        return quantity;
    }
    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
    public boolean isHandling() {
        return handling;
    }
    public void setHandling(boolean handling) {
        this.handling = handling;
    }
    public boolean isInspection() {
        return inspection;
    }
    public void setInspection(boolean inspection) {
        this.inspection = inspection;
    }
    public boolean isProcessing() {
        return processing;
    }
    public void setProcessing(boolean processing) {
        this.processing = processing;
    }
    public boolean isOthers() {
        return others;
    }
    public void setOthers(boolean others) {
        this.others = others;
    }
    public String getCalculationFrom() {
        return calculationFrom;
    }
    public void setCalculationFrom(String calculationFrom) {
        this.calculationFrom = calculationFrom;
    }
    public String getCalculationTo() {
        return calculationTo;
    }
    public void setCalculationTo(String calculationTo) {
        this.calculationTo = calculationTo;
    }

    
}
