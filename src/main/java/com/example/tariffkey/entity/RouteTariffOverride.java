package com.example.tariffkey.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "route_tariff_override")
public class RouteTariffOverride {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String productCode;
    private String originCountry;
    private String destCountry;
    private double ratePercent;

    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }

    public String getProductCode() {
        return productCode;
    }
    public void setProductCode(String productCode) {
        this.productCode = productCode;
    }

    public String getOriginCountry() {
        return originCountry;
    }
    public void setOriginCountry(String originCountry) {
        this.originCountry = originCountry;
    }

    public String getDestCountry() {
        return destCountry;
    }
    public void setDestCountry(String destCountry) {
        this.destCountry = destCountry;
    }

    public double getRatePercent() {
        return ratePercent;
    }
    public void setRatePercent(double ratePercent) {
        this.ratePercent = ratePercent;
    }
}
