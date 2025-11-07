package com.example.tariffkey.model;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class TariffRequest {
    @NotBlank
    private String fromCountry;

    @NotBlank
    private String toCountry;

    @NotBlank
    private String product;

    @NotNull
    @Min(1)
    private Integer quantity;

    private boolean handling;
    private boolean inspection;
    private boolean processing;
    private boolean others;
    
    // added datetime field (optional)
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
