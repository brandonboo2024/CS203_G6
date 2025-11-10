package com.example.tariffkey.service;

import com.example.tariffkey.model.TariffHistoryPoint;
import com.example.tariffkey.model.TariffHistoryRequest;
import com.example.tariffkey.model.TariffHistoryResponse;
import com.example.tariffkey.model.TariffHistorySummary;
import com.example.tariffkey.model.WitsTariff;
import com.example.tariffkey.repository.WitsTariffRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class TariffHistoryService {

    private static final int DEFAULT_LIMIT = 250;
    private static final int MIN_LIMIT = 10;
    private static final int MAX_LIMIT = 1000;

    private final WitsTariffRepository witsTariffRepository;

    public TariffHistoryService(WitsTariffRepository witsTariffRepository) {
        this.witsTariffRepository = witsTariffRepository;
    }

    public TariffHistoryResponse getHistory(TariffHistoryRequest request) {
        LocalDate today = LocalDate.now();
        LocalDate endDate = Optional.ofNullable(request.getEndDate()).orElse(today);
        LocalDate startDate = Optional.ofNullable(request.getStartDate()).orElse(endDate.minusYears(5));

        if (startDate.isAfter(endDate)) {
            LocalDate swap = startDate;
            startDate = endDate;
            endDate = swap;
        }

        int limit = Optional.ofNullable(request.getLimit()).orElse(DEFAULT_LIMIT);
        limit = Math.min(Math.max(limit, MIN_LIMIT), MAX_LIMIT);

        Integer startYear = startDate.getYear();
        Integer endYear = endDate.getYear();
        String product = normalizeCode(request.getProductCode());
        String origin = normalizeCode(request.getOriginCountry());
        String dest = normalizeCode(request.getDestCountry());

        Pageable pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.ASC, "year"));
        Page<WitsTariff> page = witsTariffRepository.findHistory(product, origin, dest, startYear, endYear, pageable);
        List<TariffHistoryPoint> points = page.getContent().stream()
                .map(t -> TariffHistoryPoint.builder()
                        .productCode(t.getProductCode())
                        .originCountry(t.getReporterIso())
                        .destCountry(t.getPartnerCode())
                        .year(t.getYear())
                        .validFrom(LocalDate.of(t.getYear(), 1, 1))
                        .validTo(LocalDate.of(t.getYear(), 12, 31))
                        .ratePercent(resolveRate(t))
                        .build())
                .toList();

        TariffHistorySummary summary = summarize(points);
        return TariffHistoryResponse.builder()
                .data(points)
                .summary(summary)
                .build();
    }

    private TariffHistorySummary summarize(List<TariffHistoryPoint> points) {
        if (points.isEmpty()) {
            return TariffHistorySummary.builder()
                    .totalRecords(0)
                    .build();
        }

        List<BigDecimal> rates = points.stream()
                .map(TariffHistoryPoint::getRatePercent)
                .filter(Objects::nonNull)
                .toList();

        BigDecimal min = rates.stream().min(BigDecimal::compareTo).orElse(null);
        BigDecimal max = rates.stream().max(BigDecimal::compareTo).orElse(null);
        BigDecimal avg = rates.isEmpty()
                ? null
                : rates.stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(rates.size()), 4, RoundingMode.HALF_UP);

        BigDecimal startRate = points.get(0).getRatePercent();
        BigDecimal endRate = points.get(points.size() - 1).getRatePercent();
        BigDecimal delta = (startRate != null && endRate != null)
                ? endRate.subtract(startRate)
                : null;

        return TariffHistorySummary.builder()
                .totalRecords(points.size())
                .averageRate(avg)
                .minRate(min)
                .maxRate(max)
                .startRate(startRate)
                .endRate(endRate)
                .deltaRate(delta)
                .build();
    }

    private String normalizeCode(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed.toUpperCase();
    }

    private BigDecimal resolveRate(WitsTariff tariff) {
        if (tariff.getSimpleAverage() != null) {
            return tariff.getSimpleAverage();
        }
        if (tariff.getSumOfRates() != null && tariff.getTotalNoOfLines() != null && tariff.getTotalNoOfLines() > 0) {
            return tariff.getSumOfRates()
                    .divide(BigDecimal.valueOf(tariff.getTotalNoOfLines()), 4, RoundingMode.HALF_UP);
        }
        if (tariff.getMaxRate() != null) {
            return tariff.getMaxRate();
        }
        if (tariff.getMinRate() != null) {
            return tariff.getMinRate();
        }
        return BigDecimal.ZERO;
    }
}
