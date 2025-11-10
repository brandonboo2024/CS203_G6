package com.example.tariffkey.model;

import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class TariffHistoryResponse {
    List<TariffHistoryPoint> data;
    TariffHistorySummary summary;
}
