package com.example.tariffkey.model;

import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;

@Value
@Builder
public class TariffHistorySummary {
    int totalRecords;
    BigDecimal averageRate;
    BigDecimal minRate;
    BigDecimal maxRate;
    BigDecimal startRate;
    BigDecimal endRate;
    BigDecimal deltaRate;
}
