package com.example.tariffkey.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "fee_schedule")
public class FeeSchedule {

    @Id
    @Column(name = "code", nullable = false, length = 40)
    private String code;

    @Column(name = "amount", nullable = false)
    private BigDecimal amount;
}
