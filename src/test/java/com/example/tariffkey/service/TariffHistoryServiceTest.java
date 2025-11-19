package com.example.tariffkey.service;

import com.example.tariffkey.model.TariffHistoryPoint;
import com.example.tariffkey.model.TariffHistoryRequest;
import com.example.tariffkey.model.TariffHistoryResponse;
import com.example.tariffkey.model.TariffHistorySummary;
import com.example.tariffkey.model.WitsTariff;
import com.example.tariffkey.repository.WitsTariffRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TariffHistoryServiceTest {

    @Mock
    private WitsTariffRepository witsTariffRepository;

    @InjectMocks
    private TariffHistoryService tariffHistoryService;

    private WitsTariff tariff1;
    private WitsTariff tariff2;
    private WitsTariff tariff3;

    @BeforeEach
    void setUp() {
        tariff1 = createWitsTariff("PROD1", "USA", "CHN", 2020, 
            new BigDecimal("5.00"), new BigDecimal("30.00"), 6, 
            new BigDecimal("8.00"), new BigDecimal("2.00"));
        
        tariff2 = createWitsTariff("PROD1", "USA", "CHN", 2021, 
            new BigDecimal("6.00"), null, null, 
            new BigDecimal("10.00"), new BigDecimal("3.00"));
        
        tariff3 = createWitsTariff("PROD1", "USA", "CHN", 2022, 
            null, new BigDecimal("42.00"), 6, 
            new BigDecimal("9.00"), new BigDecimal("4.00"));
    }

    @Test
    void getHistory_WithValidRequest_ShouldReturnHistoryResponse() {
        // Given
        TariffHistoryRequest request = new TariffHistoryRequest();
        request.setProductCode("prod1");
        request.setOriginCountry("usa");
        request.setDestCountry("chn");
        request.setStartDate(LocalDate.of(2020, 1, 1));
        request.setEndDate(LocalDate.of(2022, 12, 31));
        request.setLimit(100);

        List<WitsTariff> tariffs = Arrays.asList(tariff1, tariff2, tariff3);
        Page<WitsTariff> page = new PageImpl<>(tariffs);
        
        when(witsTariffRepository.findHistory(
            eq("PROD1"), eq("USA"), eq("CHN"), eq(2020), eq(2022), 
            any(Pageable.class))
        ).thenReturn(page);

        // When
        TariffHistoryResponse response = tariffHistoryService.getHistory(request);

        // Then
        assertNotNull(response);
        assertNotNull(response.getData());
        assertNotNull(response.getSummary());
        
        assertEquals(3, response.getData().size());
        assertEquals(3, response.getSummary().getTotalRecords());
        
        // Verify data points
        TariffHistoryPoint point1 = response.getData().get(0);
        assertEquals("PROD1", point1.getProductCode());
        assertEquals("USA", point1.getOriginCountry());
        assertEquals("CHN", point1.getDestCountry());
        assertEquals(2020, point1.getYear());
        assertEquals(0, new BigDecimal("5.00").compareTo(point1.getRatePercent()));

        // Verify summary calculations - use compareTo for BigDecimal
        TariffHistorySummary summary = response.getSummary();
        assertEquals(0, new BigDecimal("5.00").compareTo(summary.getMinRate()));
        assertEquals(0, new BigDecimal("7.00").compareTo(summary.getMaxRate()));
        assertEquals(0, new BigDecimal("6.00").compareTo(summary.getAverageRate()));
        assertEquals(0, new BigDecimal("5.00").compareTo(summary.getStartRate()));
        assertEquals(0, new BigDecimal("7.00").compareTo(summary.getEndRate()));
        assertEquals(0, new BigDecimal("2.00").compareTo(summary.getDeltaRate()));
    }

    @Test
    void getHistory_WithEmptyResults_ShouldReturnEmptyResponse() {
        // Given
        TariffHistoryRequest request = new TariffHistoryRequest();
        request.setProductCode("UNKNOWN");
        request.setOriginCountry("USA");
        request.setDestCountry("CHN");

        Page<WitsTariff> page = new PageImpl<>(List.of());
        
        when(witsTariffRepository.findHistory(
            eq("UNKNOWN"), eq("USA"), eq("CHN"), anyInt(), anyInt(), 
            any(Pageable.class))
        ).thenReturn(page);

        // When
        TariffHistoryResponse response = tariffHistoryService.getHistory(request);

        // Then
        assertNotNull(response);
        assertTrue(response.getData().isEmpty());
        assertEquals(0, response.getSummary().getTotalRecords());
        assertNull(response.getSummary().getAverageRate());
        assertNull(response.getSummary().getMinRate());
        assertNull(response.getSummary().getMaxRate());
        assertNull(response.getSummary().getStartRate());
        assertNull(response.getSummary().getEndRate());
        assertNull(response.getSummary().getDeltaRate());
    }

    @Test
    void getHistory_ShouldNormalizeCodes() {
        // Given
        TariffHistoryRequest request = new TariffHistoryRequest();
        request.setProductCode("  prod1  ");
        request.setOriginCountry(" usa ");
        request.setDestCountry(" chn ");

        List<WitsTariff> tariffs = Arrays.asList(tariff1);
        Page<WitsTariff> page = new PageImpl<>(tariffs);
        
        when(witsTariffRepository.findHistory(
            eq("PROD1"), eq("USA"), eq("CHN"), anyInt(), anyInt(), 
            any(Pageable.class))
        ).thenReturn(page);

        // When
        TariffHistoryResponse response = tariffHistoryService.getHistory(request);

        // Then
        assertNotNull(response);
        assertEquals(1, response.getData().size());
        assertEquals("PROD1", response.getData().get(0).getProductCode());
        assertEquals("USA", response.getData().get(0).getOriginCountry());
        assertEquals("CHN", response.getData().get(0).getDestCountry());
    }

    @Test
    void getHistory_WithNullDates_ShouldUseDefaultDates() {
        // Given
        TariffHistoryRequest request = new TariffHistoryRequest();
        request.setProductCode("PROD1");
        request.setOriginCountry("USA");
        request.setDestCountry("CHN");
        // dates are null

        List<WitsTariff> tariffs = Arrays.asList(tariff1);
        Page<WitsTariff> page = new PageImpl<>(tariffs);
        
        when(witsTariffRepository.findHistory(
            eq("PROD1"), eq("USA"), eq("CHN"), anyInt(), anyInt(), 
            any(Pageable.class))
        ).thenReturn(page);

        // When
        TariffHistoryResponse response = tariffHistoryService.getHistory(request);

        // Then
        assertNotNull(response);
        assertEquals(1, response.getData().size());
    }

    @Test
    void getHistory_WithStartDateAfterEndDate_ShouldSwapDates() {
        // Given
        TariffHistoryRequest request = new TariffHistoryRequest();
        request.setProductCode("PROD1");
        request.setOriginCountry("USA");
        request.setDestCountry("CHN");
        request.setStartDate(LocalDate.of(2022, 1, 1)); // later date
        request.setEndDate(LocalDate.of(2020, 1, 1));   // earlier date

        List<WitsTariff> tariffs = Arrays.asList(tariff1);
        Page<WitsTariff> page = new PageImpl<>(tariffs);
        
        when(witsTariffRepository.findHistory(
            eq("PROD1"), eq("USA"), eq("CHN"), eq(2020), eq(2022), 
            any(Pageable.class))
        ).thenReturn(page);

        // When
        TariffHistoryResponse response = tariffHistoryService.getHistory(request);

        // Then
        assertNotNull(response);
        assertEquals(1, response.getData().size());
    }

    @Test
    void getHistory_WithNullLimit_ShouldUseDefaultLimit() {
        // Given
        TariffHistoryRequest request = new TariffHistoryRequest();
        request.setProductCode("PROD1");
        request.setOriginCountry("USA");
        request.setDestCountry("CHN");
        request.setLimit(null);

        List<WitsTariff> tariffs = Arrays.asList(tariff1);
        Page<WitsTariff> page = new PageImpl<>(tariffs);
        
        when(witsTariffRepository.findHistory(
            eq("PROD1"), eq("USA"), eq("CHN"), anyInt(), anyInt(), 
            any(Pageable.class))
        ).thenReturn(page);

        // When
        TariffHistoryResponse response = tariffHistoryService.getHistory(request);

        // Then
        assertNotNull(response);
        assertEquals(1, response.getData().size());
    }

    @Test
    void getHistory_WithNullRates_ShouldHandleZeroRatesInSummary() {
        // Given
        WitsTariff nullRateTariff = createWitsTariff("PROD1", "USA", "CHN", 2020, 
            null, null, null, null, null);
        
        TariffHistoryRequest request = new TariffHistoryRequest();
        request.setProductCode("PROD1");
        request.setOriginCountry("USA");
        request.setDestCountry("CHN");

        List<WitsTariff> tariffs = Arrays.asList(nullRateTariff);
        Page<WitsTariff> page = new PageImpl<>(tariffs);
        
        when(witsTariffRepository.findHistory(
            eq("PROD1"), eq("USA"), eq("CHN"), anyInt(), anyInt(), 
            any(Pageable.class))
        ).thenReturn(page);

        // When
        TariffHistoryResponse response = tariffHistoryService.getHistory(request);

        // Then
        assertNotNull(response);
        assertEquals(1, response.getData().size());
        assertEquals(0, BigDecimal.ZERO.compareTo(response.getData().get(0).getRatePercent()));
        
        TariffHistorySummary summary = response.getSummary();
        assertEquals(1, summary.getTotalRecords());
        // Since resolveRate returns BigDecimal.ZERO for null rates, we should get zero values, not null
        assertEquals(0, BigDecimal.ZERO.compareTo(summary.getAverageRate()));
        assertEquals(0, BigDecimal.ZERO.compareTo(summary.getMinRate()));
        assertEquals(0, BigDecimal.ZERO.compareTo(summary.getMaxRate()));
        assertEquals(0, BigDecimal.ZERO.compareTo(summary.getStartRate()));
        assertEquals(0, BigDecimal.ZERO.compareTo(summary.getEndRate()));
        assertEquals(0, BigDecimal.ZERO.compareTo(summary.getDeltaRate()));
    }

    @Test
    void getHistory_WithMixedNullAndValidRates_ShouldHandleCorrectly() {
        // Given - One tariff with null rates, one with valid rate
        WitsTariff nullRateTariff = createWitsTariff("PROD1", "USA", "CHN", 2020, 
            null, null, null, null, null);
        WitsTariff validRateTariff = createWitsTariff("PROD1", "USA", "CHN", 2021, 
            new BigDecimal("10.00"), null, null, null, null);
        
        TariffHistoryRequest request = new TariffHistoryRequest();
        request.setProductCode("PROD1");
        request.setOriginCountry("USA");
        request.setDestCountry("CHN");

        List<WitsTariff> tariffs = Arrays.asList(nullRateTariff, validRateTariff);
        Page<WitsTariff> page = new PageImpl<>(tariffs);
        
        when(witsTariffRepository.findHistory(
            eq("PROD1"), eq("USA"), eq("CHN"), anyInt(), anyInt(), 
            any(Pageable.class))
        ).thenReturn(page);

        // When
        TariffHistoryResponse response = tariffHistoryService.getHistory(request);

        // Then
        assertNotNull(response);
        assertEquals(2, response.getData().size());
        
        // First point should have zero rate (from null rates)
        assertEquals(0, BigDecimal.ZERO.compareTo(response.getData().get(0).getRatePercent()));
        // Second point should have the valid rate
        assertEquals(0, new BigDecimal("10.00").compareTo(response.getData().get(1).getRatePercent()));
        
        TariffHistorySummary summary = response.getSummary();
        assertEquals(2, summary.getTotalRecords());
        assertEquals(0, new BigDecimal("5.00").compareTo(summary.getAverageRate())); // (0 + 10) / 2 = 5
        assertEquals(0, BigDecimal.ZERO.compareTo(summary.getMinRate()));
        assertEquals(0, new BigDecimal("10.00").compareTo(summary.getMaxRate()));
        assertEquals(0, BigDecimal.ZERO.compareTo(summary.getStartRate()));
        assertEquals(0, new BigDecimal("10.00").compareTo(summary.getEndRate()));
        assertEquals(0, new BigDecimal("10.00").compareTo(summary.getDeltaRate()));
    }

    @Test
    void getHistory_WithLimitBelowMinimum_ShouldUseMinLimit() {
        // Given
        TariffHistoryRequest request = new TariffHistoryRequest();
        request.setProductCode("PROD1");
        request.setOriginCountry("USA");
        request.setDestCountry("CHN");
        request.setLimit(5); // Below MIN_LIMIT (10)

        List<WitsTariff> tariffs = Arrays.asList(tariff1);
        Page<WitsTariff> page = new PageImpl<>(tariffs);

        when(witsTariffRepository.findHistory(
            eq("PROD1"), eq("USA"), eq("CHN"), anyInt(), anyInt(),
            any(Pageable.class))
        ).thenReturn(page);

        // When
        TariffHistoryResponse response = tariffHistoryService.getHistory(request);

        // Then
        assertNotNull(response);
        assertEquals(1, response.getData().size());
    }

    @Test
    void getHistory_WithLimitAboveMaximum_ShouldUseMaxLimit() {
        // Given
        TariffHistoryRequest request = new TariffHistoryRequest();
        request.setProductCode("PROD1");
        request.setOriginCountry("USA");
        request.setDestCountry("CHN");
        request.setLimit(2000); // Above MAX_LIMIT (1000)

        List<WitsTariff> tariffs = Arrays.asList(tariff1);
        Page<WitsTariff> page = new PageImpl<>(tariffs);

        when(witsTariffRepository.findHistory(
            eq("PROD1"), eq("USA"), eq("CHN"), anyInt(), anyInt(),
            any(Pageable.class))
        ).thenReturn(page);

        // When
        TariffHistoryResponse response = tariffHistoryService.getHistory(request);

        // Then
        assertNotNull(response);
        assertEquals(1, response.getData().size());
    }

    @Test
    void getHistory_WithEmptyStringCodes_ShouldNormalizeToNull() {
        // Given
        TariffHistoryRequest request = new TariffHistoryRequest();
        request.setProductCode("   "); // Empty string after trim
        request.setOriginCountry("");  // Empty string
        request.setDestCountry("CHN");

        List<WitsTariff> tariffs = Arrays.asList(tariff1);
        Page<WitsTariff> page = new PageImpl<>(tariffs);

        when(witsTariffRepository.findHistory(
            isNull(), isNull(), eq("CHN"), anyInt(), anyInt(),
            any(Pageable.class))
        ).thenReturn(page);

        // When
        TariffHistoryResponse response = tariffHistoryService.getHistory(request);

        // Then
        assertNotNull(response);
        assertEquals(1, response.getData().size());
    }

    @Test
    void getHistory_WithSumOfRatesFallback_ShouldCalculateCorrectly() {
        // Given - Only sumOfRates and totalNoOfLines available
        WitsTariff tariffWithSum = createWitsTariff("PROD1", "USA", "CHN", 2020,
            null, new BigDecimal("60.00"), 4, null, null); // 60/4 = 15

        TariffHistoryRequest request = new TariffHistoryRequest();
        request.setProductCode("PROD1");
        request.setOriginCountry("USA");
        request.setDestCountry("CHN");

        List<WitsTariff> tariffs = Arrays.asList(tariffWithSum);
        Page<WitsTariff> page = new PageImpl<>(tariffs);

        when(witsTariffRepository.findHistory(
            eq("PROD1"), eq("USA"), eq("CHN"), anyInt(), anyInt(),
            any(Pageable.class))
        ).thenReturn(page);

        // When
        TariffHistoryResponse response = tariffHistoryService.getHistory(request);

        // Then
        assertNotNull(response);
        assertEquals(1, response.getData().size());
        assertEquals(0, new BigDecimal("15.0000").compareTo(response.getData().get(0).getRatePercent()));
    }

    @Test
    void getHistory_WithMaxRateFallback_ShouldUseMaxRate() {
        // Given - Only maxRate available (no simpleAverage or sumOfRates)
        WitsTariff tariffWithMax = createWitsTariff("PROD1", "USA", "CHN", 2020,
            null, null, null, new BigDecimal("12.50"), null);

        TariffHistoryRequest request = new TariffHistoryRequest();
        request.setProductCode("PROD1");
        request.setOriginCountry("USA");
        request.setDestCountry("CHN");

        List<WitsTariff> tariffs = Arrays.asList(tariffWithMax);
        Page<WitsTariff> page = new PageImpl<>(tariffs);

        when(witsTariffRepository.findHistory(
            eq("PROD1"), eq("USA"), eq("CHN"), anyInt(), anyInt(),
            any(Pageable.class))
        ).thenReturn(page);

        // When
        TariffHistoryResponse response = tariffHistoryService.getHistory(request);

        // Then
        assertNotNull(response);
        assertEquals(1, response.getData().size());
        assertEquals(0, new BigDecimal("12.50").compareTo(response.getData().get(0).getRatePercent()));
    }

    @Test
    void getHistory_WithMinRateFallback_ShouldUseMinRate() {
        // Given - Only minRate available (no other rates)
        WitsTariff tariffWithMin = createWitsTariff("PROD1", "USA", "CHN", 2020,
            null, null, null, null, new BigDecimal("3.75"));

        TariffHistoryRequest request = new TariffHistoryRequest();
        request.setProductCode("PROD1");
        request.setOriginCountry("USA");
        request.setDestCountry("CHN");

        List<WitsTariff> tariffs = Arrays.asList(tariffWithMin);
        Page<WitsTariff> page = new PageImpl<>(tariffs);

        when(witsTariffRepository.findHistory(
            eq("PROD1"), eq("USA"), eq("CHN"), anyInt(), anyInt(),
            any(Pageable.class))
        ).thenReturn(page);

        // When
        TariffHistoryResponse response = tariffHistoryService.getHistory(request);

        // Then
        assertNotNull(response);
        assertEquals(1, response.getData().size());
        assertEquals(0, new BigDecimal("3.75").compareTo(response.getData().get(0).getRatePercent()));
    }

    @Test
    void getHistory_WithZeroTotalNoOfLines_ShouldSkipSumCalculation() {
        // Given - sumOfRates present but totalNoOfLines is 0 (division by zero protection)
        WitsTariff tariffWithZeroLines = createWitsTariff("PROD1", "USA", "CHN", 2020,
            null, new BigDecimal("100.00"), 0, new BigDecimal("20.00"), null);

        TariffHistoryRequest request = new TariffHistoryRequest();
        request.setProductCode("PROD1");
        request.setOriginCountry("USA");
        request.setDestCountry("CHN");

        List<WitsTariff> tariffs = Arrays.asList(tariffWithZeroLines);
        Page<WitsTariff> page = new PageImpl<>(tariffs);

        when(witsTariffRepository.findHistory(
            eq("PROD1"), eq("USA"), eq("CHN"), anyInt(), anyInt(),
            any(Pageable.class))
        ).thenReturn(page);

        // When
        TariffHistoryResponse response = tariffHistoryService.getHistory(request);

        // Then
        assertNotNull(response);
        assertEquals(1, response.getData().size());
        // Should use maxRate fallback since sumOfRates/0 is skipped
        assertEquals(0, new BigDecimal("20.00").compareTo(response.getData().get(0).getRatePercent()));
    }

    private WitsTariff createWitsTariff(String productCode, String reporterIso, String partnerCode,
                                      Integer year, BigDecimal simpleAverage, BigDecimal sumOfRates,
                                      Integer totalNoOfLines, BigDecimal maxRate, BigDecimal minRate) {
        WitsTariff tariff = new WitsTariff();
        tariff.setProductCode(productCode);
        tariff.setReporterIso(reporterIso);
        tariff.setPartnerCode(partnerCode);
        tariff.setYear(year);
        tariff.setSimpleAverage(simpleAverage);
        tariff.setSumOfRates(sumOfRates);
        tariff.setTotalNoOfLines(totalNoOfLines);
        tariff.setMaxRate(maxRate);
        tariff.setMinRate(minRate);
        return tariff;
    }
}