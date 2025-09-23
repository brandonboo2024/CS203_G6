package com.example.tariffkey.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "product_tariff_default")
public class ProductTariffDefault {
    @Id
    private String productCode;
    private double ratePercent;

    public String getProductCode() {
        return productCode;
    }
    public void setProductCode(String productCode) {
        this.productCode = productCode;
    }

    public double getRatePercent() {
        return ratePercent;
    }
    public void setRatePercent(double ratePercent) {
        this.ratePercent = ratePercent;
    }
}
