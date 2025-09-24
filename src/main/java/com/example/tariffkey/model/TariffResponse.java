package com.example.tariffkey.model;

public class TariffResponse {
    private double itemPrice;
    private double tariffRate;
    private double tariffAmount;
    private double handlingFee;
    private double inspectionFee;
    private double processingFee;
    private double otherFees;
    private double totalPrice;

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
    public double getTotalPrice() {
        return totalPrice;
    }
    public void setTotalPrice(double totalPrice) {
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

}
