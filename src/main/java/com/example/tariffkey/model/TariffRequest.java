package com.example.tariffkey.model;

public class TariffRequest {
    private String fromCountry;
    private String toCountry;
    private String product;
    // private double itemPrice;
    // private double tariffRate; // e.g., 0.15 for 15%

    // public double getItemPrice() {
    //     return itemPrice;
    // }
    // public void setItemPrice(double itemPrice) {
    //     this.itemPrice = itemPrice;
    // }
    // public double getTariffRate() {
    //     return tariffRate;
    // }
    // public void setTariffRate(double tariffRate) {
    //     this.tariffRate = tariffRate;
    // }
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
    
}
