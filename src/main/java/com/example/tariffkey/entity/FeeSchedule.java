package com.example.tariffkey.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "fee_schedule")
public class FeeSchedule {
    @Id
    @Enumerated(EnumType.STRING)
    private FeeType fee;
    private double amount;

    public FeeType getFee() {
        return fee;
    }
    public void setFee(FeeType fee) {
        this.fee = fee;
    }

    public double getAmount() {
        return amount;
    }
    public void setAmount(double amount) {
        this.amount = amount;
    }
}
