package com.example.tariffkey.model;

public class TariffRequest {
    private double itemPrice;
    private double tariffRate; // e.g., 0.15 for 15%

    public double getItemPrice() {
        return itemPrice;
    }
    public void setItemPrice(double itemPrice) {
        this.itemPrice = itemPrice;
    }
    public double getTariffRate() {
        return tariffRate;
    }
    public void setTariffRate(double tariffRate) {
        this.tariffRate = tariffRate;
    }
}
