package com.example.tariffkey.model;

public class TariffResponse {
    private double basePrice;
    private double tariffPercent;
    private double tariffAmount;
    private double feeTotal;
    private double finalCost;
    private String currency = "SGD";

    public double getBasePrice() {
        return basePrice;
    }
    public void setBasePrice(double basePrice) {
        this.basePrice = basePrice;
    }

    public double getTariffPercent() {
        return tariffPercent;
    }
    public void setTariffPercent(double tariffPercent) {
        this.tariffPercent = tariffPercent;
    }

    public double getTariffAmount() {
        return tariffAmount;
    }
    public void setTariffAmount(double tariffAmount) {
        this.tariffAmount = tariffAmount;
    }

    public double getFeeTotal() {
        return feeTotal;
    }
    public void setFeeTotal(double feeTotal) {
        this.feeTotal = feeTotal;
    }

    public double getFinalCost() {
        return finalCost;
    }
    public void setFinalCost(double finalCost) {
        this.finalCost = finalCost;
    }

    public String getCurrency() {
        return currency;
    }
    public void setCurrency(String currency) {
        this.currency = currency;
    }
}
