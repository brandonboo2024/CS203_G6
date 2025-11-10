package com.example.tariffkey.model;

import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.time.LocalDate;

@Value
@Builder
public class TariffHistoryPoint {
    String productCode;
    String originCountry;
    String destCountry;
    Integer year;
    LocalDate validFrom;
    LocalDate validTo;
    BigDecimal ratePercent;
}
