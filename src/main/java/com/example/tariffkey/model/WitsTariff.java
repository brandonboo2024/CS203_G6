package com.example.tariffkey.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "wits_tariffs")
public class WitsTariff {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nomen_code", nullable = false, length = 10)
    private String nomenCode;

    @Column(name = "reporter_iso", nullable = false, length = 10)
    private String reporterIso;

    @Column(name = "partner_code", nullable = false, length = 10)
    private String partnerCode;

    @Column(name = "product_code", nullable = false, length = 20)
    private String productCode;

    @Column(name = "\"year\"", nullable = false)
    private Integer year;

    @Column(name = "sum_of_rates")
    private BigDecimal sumOfRates;

    @Column(name = "min_rate")
    private BigDecimal minRate;

    @Column(name = "max_rate")
    private BigDecimal maxRate;

    @Column(name = "simple_average")
    private BigDecimal simpleAverage;

    @Column(name = "total_no_of_lines")
    private Integer totalNoOfLines;

    @Column(name = "nbr_pref_lines")
    private Integer nbrPrefLines;

    @Column(name = "nbr_mfn_lines")
    private Integer nbrMfnLines;

    @Column(name = "nbr_na_lines")
    private Integer nbrNaLines;

    @Column(name = "est_code", length = 10)
    private String estCode;

    @Column(name = "source_file", nullable = false)
    private String sourceFile;
}
