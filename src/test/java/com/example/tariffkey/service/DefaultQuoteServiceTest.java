package com.example.tariffkey.service;

import com.example.tariffkey.exception.TariffNotFoundException;
import com.example.tariffkey.model.*;
import com.example.tariffkey.repository.FeeScheduleRepository;
import com.example.tariffkey.repository.ProductRepository;
import com.example.tariffkey.repository.TariffRepository;
import com.example.tariffkey.repository.WitsTariffRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DefaultQuoteServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private FeeScheduleRepository feeScheduleRepository;

    @Mock
    private TariffRepository tariffRepository;

    @Mock
    private WitsTariffRepository witsTariffRepository;

    @InjectMocks
    private DefaultQuoteService defaultQuoteService;

    private Product testProduct;
    private Tariff testTariff;
    private WitsTariff testWitsTariff;
    private FeeSchedule testFeeSchedule;

    @BeforeEach
    void setUp() {
        testProduct = new Product();
        testProduct.setId(1L);
        testProduct.setCode("TEST001");
        testProduct.setHsCode("123456");
        testProduct.setBasePrice(100.0);

        testTariff = new Tariff();
        testTariff.setId(1L);
        testTariff.setOriginCountry("USA");
        testTariff.setDestinationCountry("CHN");
        testTariff.setProduct("123456");
        testTariff.setRate(0.05);
        testTariff.setLabel("Admin Tariff");
        testTariff.setValidFrom(LocalDate.of(2023, 1, 1));
        testTariff.setValidTo(LocalDate.of(2023, 12, 31));

        testWitsTariff = new WitsTariff();
        testWitsTariff.setId(1L);
        testWitsTariff.setReporterIso("USA");
        testWitsTariff.setPartnerCode("CHN");
        testWitsTariff.setProductCode("123456");
        testWitsTariff.setYear(2023);
        testWitsTariff.setSimpleAverage(new BigDecimal("5.00"));
        testWitsTariff.setEstCode("MFN");
        testWitsTariff.setNomenCode("HS2022");
        testWitsTariff.setSourceFile("wits_data.csv");

        testFeeSchedule = new FeeSchedule();
        testFeeSchedule.setCode("handling");
        testFeeSchedule.setAmount(new BigDecimal("25.00"));
    }

    @Test
    void fetchQuote_WithAdminTariff_ShouldReturnAdminTariffResponse() {
        // Given
        TariffApiRequest request = new TariffApiRequest();
        request.setOriginCountry("USA");
        request.setDestCountry("CHN");
        request.setHs6("123456");
        request.setYear("2023");

        when(tariffRepository.findActiveTariffs(
                eq("USA"), eq("CHN"), eq("123456"), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(List.of(testTariff));

        // When
        TariffApiResponse response = defaultQuoteService.fetchQuote(request);

        // Then
        assertNotNull(response);
        assertEquals(200, response.getHttpStatus());
        assertEquals(0.05, response.getTariffRate());
        assertTrue(response.isFromCache());
        assertEquals("Admin Tariff", response.getLabel());
        assertEquals("admin:1", response.getSourceLabel());
        assertEquals(testTariff.getValidFrom(), response.getValidFrom());
        assertEquals(testTariff.getValidTo(), response.getValidTo());
        assertEquals(testTariff.getId(), response.getAdminTariffId());

        verify(witsTariffRepository, never()).findFirstByReporterIsoAndPartnerCodeAndProductCodeAndYearOrderByIdAsc(any(), any(), any(), any());
        verify(witsTariffRepository, never()).findFirstByReporterIsoAndPartnerCodeAndProductCodeOrderByYearDesc(any(), any(), any());
    }

    @Test
    void fetchQuote_WithWitsTariffByYear_ShouldReturnWitsResponse() {
        // Given
        TariffApiRequest request = new TariffApiRequest();
        request.setOriginCountry("USA");
        request.setDestCountry("CHN");
        request.setHs6("123456");
        request.setYear("2023");

        when(tariffRepository.findActiveTariffs(any(), any(), any(), any(), any())).thenReturn(List.of());
        when(tariffRepository.findTopByOriginCountryAndDestinationCountryAndProductOrderByValidFromDesc(any(), any(), any()))
                .thenReturn(Optional.empty());
        when(witsTariffRepository.findFirstByReporterIsoAndPartnerCodeAndProductCodeAndYearOrderByIdAsc(
                eq("USA"), eq("CHN"), eq("123456"), eq(2023)))
                .thenReturn(Optional.of(testWitsTariff));

        // When
        TariffApiResponse response = defaultQuoteService.fetchQuote(request);

        // Then
        assertNotNull(response);
        assertEquals(200, response.getHttpStatus());
        assertEquals(0.05, response.getTariffRate()); // 5.00% converted to 0.05
        assertTrue(response.isFromCache());
        assertEquals("Dataset tariff rate", response.getLabel());
        assertEquals("HS2022", response.getSourceLabel());
        assertEquals(2023, response.getYear());
        assertArrayEquals(new String[]{"MFN"}, response.getTariffTypes());
        assertEquals("HS2022", response.getNomenclature());
        assertEquals("wits_tariffs:wits_data.csv", response.getUrl());
    }

    @Test
    void fetchQuote_WithWitsTariffLatest_ShouldReturnWitsResponse() {
        // Given
        TariffApiRequest request = new TariffApiRequest();
        request.setOriginCountry("USA");
        request.setDestCountry("CHN");
        request.setHs6("123456");
        request.setYear("ALL");

        when(tariffRepository.findActiveTariffs(any(), any(), any(), any(), any())).thenReturn(List.of());
        when(tariffRepository.findTopByOriginCountryAndDestinationCountryAndProductOrderByValidFromDesc(any(), any(), any()))
                .thenReturn(Optional.empty());
        // Only mock the method that will actually be called
        when(witsTariffRepository.findFirstByReporterIsoAndPartnerCodeAndProductCodeOrderByYearDesc(
                eq("USA"), eq("CHN"), eq("123456")))
                .thenReturn(Optional.of(testWitsTariff));

        // When
        TariffApiResponse response = defaultQuoteService.fetchQuote(request);

        // Then
        assertNotNull(response);
        assertEquals(200, response.getHttpStatus());
        assertEquals(0.05, response.getTariffRate());
    }

    @Test
    void fetchQuote_WithNoTariffFound_ShouldThrowException() {
        // Given
        TariffApiRequest request = new TariffApiRequest();
        request.setOriginCountry("USA");
        request.setDestCountry("CHN");
        request.setHs6("123456");
        request.setYear("2023");

        when(tariffRepository.findActiveTariffs(any(), any(), any(), any(), any())).thenReturn(List.of());
        when(tariffRepository.findTopByOriginCountryAndDestinationCountryAndProductOrderByValidFromDesc(any(), any(), any()))
                .thenReturn(Optional.empty());
        when(witsTariffRepository.findFirstByReporterIsoAndPartnerCodeAndProductCodeAndYearOrderByIdAsc(any(), any(), any(), any()))
                .thenReturn(Optional.empty());
        when(witsTariffRepository.findFirstByReporterIsoAndPartnerCodeAndProductCodeOrderByYearDesc(any(), any(), any()))
                .thenReturn(Optional.empty());

        // When & Then
        assertThrows(TariffNotFoundException.class, () -> defaultQuoteService.fetchQuote(request));
    }

    @Test
    void fetchQuote_WithWindowDates_ShouldUseProvidedDates() {
        // Given
        TariffApiRequest request = new TariffApiRequest();
        request.setOriginCountry("USA");
        request.setDestCountry("CHN");
        request.setHs6("123456");
        
        LocalDate windowStart = LocalDate.of(2023, 6, 1);
        LocalDate windowEnd = LocalDate.of(2023, 6, 30);

        when(tariffRepository.findActiveTariffs(
                eq("USA"), eq("CHN"), eq("123456"), eq(windowStart), eq(windowEnd)))
                .thenReturn(List.of(testTariff));

        // When
        TariffApiResponse response = defaultQuoteService.fetchQuote(request, windowStart, windowEnd);

        // Then
        assertNotNull(response);
        assertEquals(0.05, response.getTariffRate());
    }

    @Test
    void fetchQuote_WithSwappedWindowDates_ShouldSwapDates() {
        // Given
        TariffApiRequest request = new TariffApiRequest();
        request.setOriginCountry("USA");
        request.setDestCountry("CHN");
        request.setHs6("123456");
        
        LocalDate windowStart = LocalDate.of(2023, 6, 30); // Later date
        LocalDate windowEnd = LocalDate.of(2023, 6, 1);   // Earlier date

        when(tariffRepository.findActiveTariffs(
                eq("USA"), eq("CHN"), eq("123456"), eq(LocalDate.of(2023, 6, 1)), eq(LocalDate.of(2023, 6, 30))))
                .thenReturn(List.of(testTariff));

        // When
        TariffApiResponse response = defaultQuoteService.fetchQuote(request, windowStart, windowEnd);

        // Then
        assertNotNull(response);
        assertEquals(0.05, response.getTariffRate());
    }

    @Test
    void calculateQuote_WithValidRequest_ShouldReturnCompleteResponse() {
        // Given
        TariffRequest request = new TariffRequest();
        request.setFromCountry("USA");
        request.setToCountry("CHN");
        request.setHsCode("123456");
        request.setProduct("TEST001");
        request.setQuantity(10);
        request.setCustomBasePrice(100.0);
        request.setHandling(true);
        request.setInspection(true);
        request.setProcessing(false);
        request.setOthers(false);

        when(productRepository.findByCode("TEST001")).thenReturn(Optional.of(testProduct));
        when(tariffRepository.findActiveTariffs(any(), any(), any(), any(), any())).thenReturn(List.of(testTariff));
        when(feeScheduleRepository.findById("handling")).thenReturn(Optional.of(testFeeSchedule));
        
        FeeSchedule inspectionFee = new FeeSchedule();
        inspectionFee.setCode("inspection");
        inspectionFee.setAmount(new BigDecimal("15.00"));
        when(feeScheduleRepository.findById("inspection")).thenReturn(Optional.of(inspectionFee));

        // When
        TariffResponse response = defaultQuoteService.calculateQuote(request);

        // Then
        assertNotNull(response);
        assertFalse(response.isPriceRequired());
        assertEquals(1000.0, response.getItemPrice()); // 10 * 100.0
        assertEquals(5.0, response.getTariffRate()); // 0.05 * 100
        assertEquals(50.0, response.getTariffAmount()); // 1000 * 0.05
        assertEquals(25.0, response.getHandlingFee());
        assertEquals(15.0, response.getInspectionFee());
        assertEquals(0.0, response.getProcessingFee());
        assertEquals(0.0, response.getOtherFees());
        assertEquals(1090.0, response.getTotalPrice()); // 1000 + 50 + 25 + 15
        assertEquals("Admin Tariff", response.getLabel());
        assertEquals("admin:1", response.getSource());
        assertEquals(testTariff.getId(), response.getAdminTariffId());
        assertNotNull(response.getValidFrom());
        assertNotNull(response.getValidTo());
    }

    @Test
    void calculateQuote_WithPriceRequired_ShouldReturnPriceRequiredResponse() {
        // Given
        TariffRequest request = new TariffRequest();
        request.setFromCountry("USA");
        request.setToCountry("CHN");
        request.setHsCode("123456");
        request.setProduct("TEST001");
        request.setQuantity(10);
        // customBasePrice is null

        when(productRepository.findByCode("TEST001")).thenReturn(Optional.of(testProduct));
        when(tariffRepository.findActiveTariffs(any(), any(), any(), any(), any())).thenReturn(List.of(testTariff));

        // When
        TariffResponse response = defaultQuoteService.calculateQuote(request);

        // Then
        assertNotNull(response);
        assertTrue(response.isPriceRequired());
        assertEquals("TEST001", response.getMissingProduct());
        assertEquals("123456", response.getMissingHsCode());
        assertEquals(5.0, response.getTariffRate());
        assertEquals("Base price required for HS 123456 before totals can be calculated.", response.getMessage());
        assertEquals(100.0, response.getSuggestedBasePrice());
    }

    @Test
    void calculateQuote_WithInvalidQuantity_ShouldThrowException() {
        // Given
        TariffRequest request = new TariffRequest();
        request.setFromCountry("USA");
        request.setToCountry("CHN");
        request.setHsCode("123456");
        request.setQuantity(0); // Invalid quantity
        request.setCustomBasePrice(100.0);

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> defaultQuoteService.calculateQuote(request));
    }

    @Test
    void calculateQuote_WithApiError_ShouldThrowException() {
        // Given
        TariffRequest request = new TariffRequest();
        request.setFromCountry("USA");
        request.setToCountry("CHN");
        request.setHsCode("123456");
        request.setQuantity(10);
        request.setCustomBasePrice(100.0);

        // When & Then - Should throw TariffNotFoundException when no tariff data is found
        assertThrows(TariffNotFoundException.class, () -> defaultQuoteService.calculateQuote(request));
    }

    @Test
    void calculateQuote_WithCalculationToDate_ShouldUseYearFromDate() {
        // Given
        TariffRequest request = new TariffRequest();
        request.setFromCountry("USA");
        request.setToCountry("CHN");
        request.setHsCode("123456");
        request.setProduct("TEST001");
        request.setQuantity(10);
        request.setCustomBasePrice(100.0);
        request.setCalculationTo("2023-06-15T10:30:00Z"); // This should resolve to year "2023"

        when(productRepository.findByCode("TEST001")).thenReturn(Optional.of(testProduct));
        
        // Mock to expect year 2023 in the API request
        when(tariffRepository.findActiveTariffs(any(), any(), any(), any(), any())).thenReturn(List.of());
        when(tariffRepository.findTopByOriginCountryAndDestinationCountryAndProductOrderByValidFromDesc(any(), any(), any()))
                .thenReturn(Optional.empty());
        when(witsTariffRepository.findFirstByReporterIsoAndPartnerCodeAndProductCodeAndYearOrderByIdAsc(
                eq("USA"), eq("CHN"), eq("123456"), eq(2023)))
                .thenReturn(Optional.of(testWitsTariff));

        // When
        TariffResponse response = defaultQuoteService.calculateQuote(request);

        // Then
        assertNotNull(response);
        assertEquals(5.0, response.getTariffRate());
    }

    @Test
    void calculateQuote_WithCalculationFromDate_ShouldUseYearFromDate() {
        // Given
        TariffRequest request = new TariffRequest();
        request.setFromCountry("USA");
        request.setToCountry("CHN");
        request.setHsCode("123456");
        request.setProduct("TEST001");
        request.setQuantity(10);
        request.setCustomBasePrice(100.0);
        request.setCalculationFrom("2022-01-01T00:00:00Z"); // This should resolve to year "2022"

        when(productRepository.findByCode("TEST001")).thenReturn(Optional.of(testProduct));
        
        // Mock to expect year 2022 in the API request
        when(tariffRepository.findActiveTariffs(any(), any(), any(), any(), any())).thenReturn(List.of());
        when(tariffRepository.findTopByOriginCountryAndDestinationCountryAndProductOrderByValidFromDesc(any(), any(), any()))
                .thenReturn(Optional.empty());
        when(witsTariffRepository.findFirstByReporterIsoAndPartnerCodeAndProductCodeAndYearOrderByIdAsc(
                eq("USA"), eq("CHN"), eq("123456"), eq(2022)))
                .thenReturn(Optional.of(testWitsTariff));

        // When
        TariffResponse response = defaultQuoteService.calculateQuote(request);

        // Then
        assertNotNull(response);
        assertEquals(5.0, response.getTariffRate());
    }

    @Test
    void calculateQuote_WithNoCalculationDates_ShouldUseAllYears() {
        // Given
        TariffRequest request = new TariffRequest();
        request.setFromCountry("USA");
        request.setToCountry("CHN");
        request.setHsCode("123456");
        request.setProduct("TEST001");
        request.setQuantity(10);
        request.setCustomBasePrice(100.0);
        // No calculation dates set - should use "ALL"

        when(productRepository.findByCode("TEST001")).thenReturn(Optional.of(testProduct));
        
        // Mock to expect no specific year (latest tariff)
        when(tariffRepository.findActiveTariffs(any(), any(), any(), any(), any())).thenReturn(List.of());
        when(tariffRepository.findTopByOriginCountryAndDestinationCountryAndProductOrderByValidFromDesc(any(), any(), any()))
                .thenReturn(Optional.empty());
        when(witsTariffRepository.findFirstByReporterIsoAndPartnerCodeAndProductCodeOrderByYearDesc(
                eq("USA"), eq("CHN"), eq("123456")))
                .thenReturn(Optional.of(testWitsTariff));

        // When
        TariffResponse response = defaultQuoteService.calculateQuote(request);

        // Then
        assertNotNull(response);
        assertEquals(5.0, response.getTariffRate());
    }

    @Test
    void fetchQuote_WithNullSimpleAverage_ShouldReturnZeroRate() {
        // Given
        TariffApiRequest request = new TariffApiRequest();
        request.setOriginCountry("USA");
        request.setDestCountry("CHN");
        request.setHs6("123456");
        request.setYear("2023");

        WitsTariff witsTariffWithNullRate = new WitsTariff();
        witsTariffWithNullRate.setSimpleAverage(null);
        witsTariffWithNullRate.setYear(2023);

        when(tariffRepository.findActiveTariffs(any(), any(), any(), any(), any())).thenReturn(List.of());
        when(tariffRepository.findTopByOriginCountryAndDestinationCountryAndProductOrderByValidFromDesc(any(), any(), any()))
                .thenReturn(Optional.empty());
        when(witsTariffRepository.findFirstByReporterIsoAndPartnerCodeAndProductCodeAndYearOrderByIdAsc(any(), any(), any(), any()))
                .thenReturn(Optional.of(witsTariffWithNullRate));

        // When
        TariffApiResponse response = defaultQuoteService.fetchQuote(request);

        // Then
        assertNotNull(response);
        assertEquals(0.0, response.getTariffRate());
    }

    @Test
    void fetchQuote_WithMissingRequiredFields_ShouldThrowException() {
        // Given
        TariffApiRequest request = new TariffApiRequest();
        request.setOriginCountry("  "); // Empty origin country
        request.setDestCountry("CHN");
        request.setHs6("123456");

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> defaultQuoteService.fetchQuote(request));
    }

    @Test
    void fetchQuote_WithInvalidYearFormat_ShouldUseLatestTariff() {
        // Given
        TariffApiRequest request = new TariffApiRequest();
        request.setOriginCountry("USA");
        request.setDestCountry("CHN");
        request.setHs6("123456");
        request.setYear("INVALID_YEAR"); // Invalid year format

        when(tariffRepository.findActiveTariffs(any(), any(), any(), any(), any())).thenReturn(List.of());
        when(tariffRepository.findTopByOriginCountryAndDestinationCountryAndProductOrderByValidFromDesc(any(), any(), any()))
                .thenReturn(Optional.empty());
        when(witsTariffRepository.findFirstByReporterIsoAndPartnerCodeAndProductCodeOrderByYearDesc(
                eq("USA"), eq("CHN"), eq("123456")))
                .thenReturn(Optional.of(testWitsTariff));

        // When
        TariffApiResponse response = defaultQuoteService.fetchQuote(request);

        // Then
        assertNotNull(response);
        assertEquals(0.05, response.getTariffRate());
    }
}