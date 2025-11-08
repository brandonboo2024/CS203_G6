package com.example.tariffkey.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "wits_country_metadata")
public class WitsCountryMetadata {

    @Id
    @Column(name = "country_code", length = 10)
    private String countryCode;

    @Column(name = "iso3", length = 3)
    private String iso3;

    @Column(name = "country_name")
    private String countryName;

    @Column(name = "long_name")
    private String longName;

    @Column(name = "income_group")
    private String incomeGroup;

    @Column(name = "lending_category")
    private String lendingCategory;

    @Column(name = "region")
    private String region;

    @Column(name = "currency_unit")
    private String currencyUnit;

    @Column(name = "is_group")
    private boolean group;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;
}
