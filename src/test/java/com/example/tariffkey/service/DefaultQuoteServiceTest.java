package com.example.tariffkey.service;

import com.example.tariffkey.model.FeeSchedule;
import com.example.tariffkey.model.Product;
import com.example.tariffkey.model.Tariff;
import com.example.tariffkey.model.TariffApiRequest;
import com.example.tariffkey.model.TariffApiResponse;
import com.example.tariffkey.model.TariffRequest;
import com.example.tariffkey.model.TariffResponse;
import com.example.tariffkey.model.WitsTariff;
import com.example.tariffkey.repository.FeeScheduleRepository;
import com.example.tariffkey.repository.ProductRepository;
import com.example.tariffkey.repository.TariffRepository;
import com.example.tariffkey.repository.WitsTariffRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class DefaultQuoteServiceTest {

    @Autowired
    private DefaultQuoteService defaultQuoteService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private FeeScheduleRepository feeScheduleRepository;

    @Autowired
    private TariffRepository tariffRepository;

    @Autowired
    private WitsTariffRepository witsTariffRepository;

    @BeforeEach
    void seedData() {
        productRepository.deleteAll();
        feeScheduleRepository.deleteAll();
        tariffRepository.deleteAll();
        witsTariffRepository.deleteAll();

        productRepository.save(Product.builder()
                .code("electronics")
                .hsCode("847130")
                .basePrice(100.0)
                .build());

        feeScheduleRepository.saveAll(List.of(
                FeeSchedule.builder().code("handling").amount(BigDecimal.valueOf(10)).build(),
                FeeSchedule.builder().code("inspection").amount(BigDecimal.valueOf(20)).build(),
                FeeSchedule.builder().code("processing").amount(BigDecimal.valueOf(5)).build(),
                FeeSchedule.builder().code("others").amount(BigDecimal.valueOf(2)).build()
        ));
    }

    @Test
    void fetchQuoteReturnsCachedTariff() {
        tariffRepository.save(Tariff.builder()
                .originCountry("840")
                .destinationCountry("702")
                .product("847130")
                .rate(0.123)
                .validFrom(LocalDate.of(2023, 1, 1))
                .validTo(LocalDate.of(2023, 12, 31))
                .label("Admin tariff 2023")
                .build());

        TariffApiRequest request = TariffApiRequest.builder()
                .originCountry("840")
                .destCountry("702")
                .hs6("847130")
                .year("2021")
                .build();

        TariffApiResponse response = defaultQuoteService.fetchQuote(request);

        assertThat(response.isFromCache()).isTrue();
        assertThat(response.getTariffRate()).isEqualTo(0.123);
        assertThat(response.getHttpStatus()).isEqualTo(200);
    }

    @Test
    void fetchQuotePrefersTariffMatchingCalculationWindow() {
        tariffRepository.save(Tariff.builder()
                .originCountry("840")
                .destinationCountry("702")
                .product("847130")
                .rate(0.05)
                .validFrom(LocalDate.of(2022, 1, 1))
                .validTo(LocalDate.of(2022, 12, 31))
                .label("Legacy 2022")
                .build());

        tariffRepository.save(Tariff.builder()
                .originCountry("840")
                .destinationCountry("702")
                .product("847130")
                .rate(0.08)
                .validFrom(LocalDate.of(2024, 1, 1))
                .validTo(LocalDate.of(2024, 12, 31))
                .label("Upcoming 2024")
                .build());

        TariffApiRequest request = TariffApiRequest.builder()
                .originCountry("840")
                .destCountry("702")
                .hs6("847130")
                .year("2024")
                .build();

        TariffApiResponse response = defaultQuoteService.fetchQuote(
                request,
                LocalDate.of(2024, 3, 1),
                LocalDate.of(2024, 3, 31));

        assertThat(response.getTariffRate()).isEqualTo(0.08);
        assertThat(response.getLabel()).isEqualTo("Upcoming 2024");
        assertThat(response.getValidFrom()).isEqualTo(LocalDate.of(2024, 1, 1));
    }

    @Test
    void fetchQuoteFallsBackToWitsDatasetWhenNoManualTariff() {
        witsTariffRepository.save(WitsTariff.builder()
                .nomenCode("H0")
                .reporterIso("840")
                .partnerCode("702")
                .productCode("847130")
                .year(2021)
                .simpleAverage(BigDecimal.valueOf(7.5))
                .sourceFile("test.csv")
                .build());

        TariffApiRequest request = TariffApiRequest.builder()
                .originCountry("840")
                .destCountry("702")
                .hs6("847130")
                .year("2021")
                .build();

        TariffApiResponse response = defaultQuoteService.fetchQuote(request);

        assertThat(response.getTariffRate()).isEqualTo(0.075);
        assertThat(response.getYear()).isEqualTo(2021);
        assertThat(response.getNomenclature()).isEqualTo("H0");
    }
// commented out as no more manaual cache, all is via database queries
    // @Test
//     void calculateQuoteUsesDatasetTariffWhenManualCacheMissing() {
//         witsTariffRepository.save(WitsTariff.builder()
//                 .nomenCode("H0")
//                 .reporterIso("840")
//                 .partnerCode("702")
//                 .productCode("847130")
//                 .year(2021)
//                 .simpleAverage(BigDecimal.valueOf(7.5))
//                 .sourceFile("test.csv")
//                 .build());
//
//         TariffRequest request = new TariffRequest();
//         request.setFromCountry("840");
//         request.setToCountry("702");
//         request.setProduct("electronics");
//         request.setQuantity(4);
//         request.setHandling(true);
//         request.setInspection(true);
//         request.setProcessing(true);
//         request.setOthers(false);
//         request.setCalculationFrom("2021-01-01T00:00:00Z");
//         request.setCalculationTo("2021-12-31T00:00:00Z");
//
//         TariffResponse response = defaultQuoteService.calculateQuote(request);
//
//         assertThat(response.getItemPrice()).isEqualTo(400.0);
//         assertThat(response.getTariffRate()).isEqualTo(7.5);
//         assertThat(response.getTariffAmount()).isEqualTo(30.0);
//         assertThat(response.getHandlingFee()).isEqualTo(10.0);
//         assertThat(response.getInspectionFee()).isEqualTo(20.0);
//         assertThat(response.getProcessingFee()).isEqualTo(5.0);
//         assertThat(response.getOtherFees()).isEqualTo(0.0);
//         assertThat(response.getTotalPrice()).isEqualTo(465.0);
//         assertThat(response.getLabel()).isEqualTo("Dataset tariff rate");
//     }
// }
}
