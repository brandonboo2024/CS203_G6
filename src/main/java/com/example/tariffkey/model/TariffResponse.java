package com.example.tariffkey.model;
import java.util.List;

public class TariffResponse {
    private double itemPrice;
    private double tariffRate;
    private double tariffAmount;
    private double handlingFee;
    private double inspectionFee;
    private double processingFee;
    private double otherFees;
    private Double totalPrice;
    private List<Segment> segments;
    private String label;
    private String source;

    // this class is to include timeperiods where one tariff ends, and a new tariff starts
    public static class Segment {
        private String from;            // date time
        private String to;              // date time
        private double ratePercent;     // tariff in this segment
        private double quantityPortion; // quantity allocated to this segment
        private double itemPrice;       // portion * basePrice
        private double tariffAmount;    // itemPrice * rate
        private String label;
        private String source;

        public String getFrom() { return from; }
        public void setFrom(String from) { this.from = from; }
        public String getTo() { return to; }
        public void setTo(String to) { this.to = to; }
        public double getRatePercent() { return ratePercent; }
        public void setRatePercent(double ratePercent) { this.ratePercent = ratePercent; }
        public double getQuantityPortion() { return quantityPortion; }
        public void setQuantityPortion(double quantityPortion) { this.quantityPortion = quantityPortion; }
        public double getItemPrice() { return itemPrice; }
        public void setItemPrice(double itemPrice) { this.itemPrice = itemPrice; }
        public double getTariffAmount() { return tariffAmount; }
        public void setTariffAmount(double tariffAmount) { this.tariffAmount = tariffAmount; }
        public void setLabel(String label) { this.label = label; }
        public String getLabel() { return label; }
        public void setSource(String source) { this.source = source; }
        public String getSource() { return source;}
    }
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
    public Double getTotalPrice() {
        return totalPrice;
    }
    public void setTotalPrice(Double totalPrice) {
        this.totalPrice = totalPrice;
    }
    public double getTariffAmount() {
        return tariffAmount;
    }
    public void setTariffAmount(double tariffAmount) {
        this.tariffAmount = tariffAmount;
    }
    public double getHandlingFee() {
        return handlingFee;
    }
    public void setHandlingFee(double handlingFee) {
        this.handlingFee = handlingFee;
    }
    public double getInspectionFee() {
        return inspectionFee;
    }
    public void setInspectionFee(double inspectionFee) {
        this.inspectionFee = inspectionFee;
    }
    public double getProcessingFee() {
        return processingFee;
    }
    public void setProcessingFee(double processingFee) {
        this.processingFee = processingFee;
    }
    public double getOtherFees() {
        return otherFees;
    }
    public void setOtherFees(double otherFees) {
        this.otherFees = otherFees;
    }
    public List<Segment> getSegments() {
        return segments;
    }
    public void setSegments(List<Segment> segments) {
        this.segments = segments;
    }
    public void setLabel(String label) { this.label = label; }
    public String getLabel() { return label; }
    public void setSource(String source) { this.source = source; }
    public String getSource() { return source;}

}
