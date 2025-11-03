package com.example.tariffkey.model;

public class Tariff {
    private int id;
    private String product;
    private String originCountry;
    private String destinationCountry;
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
