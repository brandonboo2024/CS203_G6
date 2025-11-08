package com.example.tariffkey.service;

import com.example.tariffkey.model.AdminTariffRequest;
import com.example.tariffkey.model.Tariff;
import com.example.tariffkey.repository.TariffRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
class TariffManagementServiceTest {

    @Autowired
    private TariffManagementService tariffManagementService;

    @Autowired
    private TariffRepository tariffRepository;

    @BeforeEach
    void clean() {
        tariffRepository.deleteAll();
    }

    @Test
    void addTariffRejectsOverlapWithoutOverride() {
        tariffRepository.save(Tariff.builder()
                .product("847130")
                .originCountry("840")
                .destinationCountry("702")
                .rate(0.05)
                .validFrom(LocalDate.of(2023, 1, 1))
                .validTo(LocalDate.of(2023, 12, 31))
                .label("Existing")
                .build());

        AdminTariffRequest request = new AdminTariffRequest();
        request.setProduct("847130");
        request.setOriginCountry("840");
        request.setDestinationCountry("702");
        request.setRate(0.06);
        request.setValidFrom(LocalDate.of(2023, 6, 1));
        request.setValidTo(LocalDate.of(2023, 12, 31));
        request.setLabel("Overlap");
        request.setAllowOverride(false);

        assertThatThrownBy(() -> tariffManagementService.addTariff(request, "admin"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Overlapping");
    }

    @Test
    void addTariffClosesExistingRangeWhenOverrideEnabled() {
        tariffRepository.save(Tariff.builder()
                .product("847130")
                .originCountry("840")
                .destinationCountry("702")
                .rate(0.05)
                .validFrom(LocalDate.of(2023, 1, 1))
                .validTo(LocalDate.of(2023, 12, 31))
                .label("Existing")
                .build());

        AdminTariffRequest request = new AdminTariffRequest();
        request.setProduct("847130");
        request.setOriginCountry("840");
        request.setDestinationCountry("702");
        request.setRate(0.06);
        request.setValidFrom(LocalDate.of(2023, 6, 1));
        request.setValidTo(LocalDate.of(2023, 12, 31));
        request.setLabel("New window");
        request.setAllowOverride(true);

        Tariff saved = tariffManagementService.addTariff(request, "admin");

        Tariff previous = tariffRepository.findAll().stream()
                .filter(t -> !t.getId().equals(saved.getId()))
                .findFirst()
                .orElseThrow();

        assertThat(previous.getValidTo()).isEqualTo(LocalDate.of(2023, 5, 31));
        assertThat(saved.getValidFrom()).isEqualTo(LocalDate.of(2023, 6, 1));
        assertThat(saved.getCreatedBy()).isEqualTo("admin");
    }
}
