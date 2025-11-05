package com.example.tariffkey.service;

import com.example.tariffkey.model.FeeSchedule;
import com.example.tariffkey.model.Product;
import com.example.tariffkey.model.TariffApiRequest;
import com.example.tariffkey.model.TariffApiResponse;
import com.example.tariffkey.model.TariffRequest;
import com.example.tariffkey.model.TariffResponse;
import com.example.tariffkey.repository.FeeScheduleRepository;
import com.example.tariffkey.repository.ProductRepository;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class DefaultQuoteServiceTest {

    private static final String SAMPLE_RESPONSE = """
        {
          \"dataSets\": [
            {
              \"series\": {
                \"0:0:0:0\": {
                  \"observations\": {
                    \"0\": [5.0],
                    \"1\": [7.5]
                  }
                }
              }
            }
          ],
          \"structure\": {
            \"dimensions\": {
              \"observation\": [
                {
                  \"values\": [
                    { \"id\": \"2020\" },
                    { \"id\": \"2021\" }
                  ]
                }
              ]
            },
            \"attributes\": {
              \"observation\": [
                {
                  \"id\": \"NOMENCODE\",
                  \"values\": [ { \"id\": \"H5\" } ]
                },
                {
                  \"id\": \"TARIFFTYPE\",
                  \"values\": [ { \"id\": \"MFN\" } ]
                }
              ]
            }
          }
        }
        """;

    private static final MockWebServer mockWebServer = new MockWebServer();
    private static final java.util.concurrent.atomic.AtomicBoolean mockWebServerStarted = new java.util.concurrent.atomic.AtomicBoolean(false);

    @Autowired
    private DefaultQuoteService defaultQuoteService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private FeeScheduleRepository feeScheduleRepository;

    @AfterAll
    void tearDownServer() throws IOException {
        if (mockWebServerStarted.get()) {
            mockWebServer.shutdown();
            mockWebServerStarted.set(false);
        }
    }

    @DynamicPropertySource
    static void overrideBaseUrl(DynamicPropertyRegistry registry) {
        registry.add("wits.api.base-url", () -> {
            if (mockWebServerStarted.compareAndSet(false, true)) {
                try {
                    mockWebServer.start();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            return mockWebServer.url("/API/V1/SDMX/V21/datasource/TRN").toString();
        });
    }

    @BeforeEach
    void seedData() {
        productRepository.deleteAll();
        feeScheduleRepository.deleteAll();

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
    void fetchQuoteParsesLatestObservation() {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody(SAMPLE_RESPONSE));

        TariffApiRequest request = TariffApiRequest.builder()
                .originCountry("840")
                .destCountry("702")
                .hs6("847130")
                .year("2021")
                .build();

        TariffApiResponse response = defaultQuoteService.fetchQuote(request);

        assertThat(response.getHttpStatus()).isEqualTo(200);
        assertThat(response.getTariffRate()).isEqualTo(0.075);
        assertThat(response.getTariffTypes()).containsExactly("MFN");
        assertThat(response.getYear()).isEqualTo(2021);
        assertThat(response.getNomenclature()).isEqualTo("H5");
    }

    @Test
    void calculateQuoteCombinesProductPricingAndFees() {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody(SAMPLE_RESPONSE));

        TariffRequest request = new TariffRequest();
        request.setFromCountry("840");
        request.setToCountry("702");
        request.setProduct("electronics");
        request.setQuantity(4);
        request.setHandling(true);
        request.setInspection(true);
        request.setProcessing(true);
        request.setOthers(false);
        request.setCalculationFrom("2021-01-01T00:00:00Z");
        request.setCalculationTo("2021-12-31T00:00:00Z");

        TariffResponse response = defaultQuoteService.calculateQuote(request);

        assertThat(response.getItemPrice()).isEqualTo(400.0);
        assertThat(response.getTariffRate()).isEqualTo(7.5);
        assertThat(response.getTariffAmount()).isEqualTo(30.0);
        assertThat(response.getHandlingFee()).isEqualTo(10.0);
        assertThat(response.getInspectionFee()).isEqualTo(20.0);
        assertThat(response.getProcessingFee()).isEqualTo(5.0);
        assertThat(response.getOtherFees()).isEqualTo(0.0);
        assertThat(response.getTotalPrice()).isEqualTo(465.0);
        assertThat(response.getSegments()).isEmpty();
        assertThat(response.getSource()).isEqualTo("H5");
    }
}
