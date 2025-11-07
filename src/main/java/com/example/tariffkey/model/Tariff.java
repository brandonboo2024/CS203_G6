package com.example.tariffkey.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "tariffs")
public class Tariff {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String product;

    @Column(name = "origin_country", nullable = false)
    private String originCountry;

    @Column(name = "destination_country", nullable = false)
    private String destinationCountry;

    @Column(nullable = false)
    private double rate;
}
