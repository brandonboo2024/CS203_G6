package com.example.tariffkey.model;

import jakarta.validation.constraints.DecimalMin;
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
    @Pattern(regexp = "^[A-Z0-9]{2,6}$",
             message = "Country code must use 2-6 uppercase letters or digits")
    private String fromCountry;

    @NotBlank
    @Pattern(regexp = "^[A-Z0-9]{2,6}$",
             message = "Country code must use 2-6 uppercase letters or digits")
    private String toCountry;

    @NotBlank
    @Pattern(regexp = "^[A-Za-z0-9_\\-]{2,64}$",
             message = "Product code can contain letters, numbers, underscores or hyphens")
    private String product;

    @NotNull
    @Min(1)
    @Max(10000)
    private Integer quantity;

    @Pattern(regexp = "^[0-9]{4,10}$",
             message = "HS code must be numeric (4-10 digits)")
    private String hsCode;

    @DecimalMin(value = "0.01", message = "Custom base price must be at least 0.01")
    private Double customBasePrice;

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
    public String getHsCode() {
        return hsCode;
    }
    public void setHsCode(String hsCode) {
        this.hsCode = hsCode;
    }
    public Double getCustomBasePrice() {
        return customBasePrice;
    }
    public void setCustomBasePrice(Double customBasePrice) {
        this.customBasePrice = customBasePrice;
    }
}
