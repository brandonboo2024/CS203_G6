package com.example.tariffkey.service;

import com.example.tariffkey.model.AdminTariffRequest;
import com.example.tariffkey.model.Tariff;
import com.example.tariffkey.repository.TariffRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.List;

@Service
public class TariffManagementService {

    private final TariffRepository tariffRepository;

    public TariffManagementService(TariffRepository tariffRepository) {
        this.tariffRepository = tariffRepository;
    }

    public List<Tariff> getAllTariffs() {
        return tariffRepository.findAll(Sort.by(Sort.Direction.DESC, "validFrom", "id"));
    }

    public Tariff addTariff(AdminTariffRequest request, String createdBy) {
        validateRequest(request);
        LocalDate validFrom = request.getValidFrom();
        LocalDate validTo = request.getValidTo();

        List<Tariff> overlapping = tariffRepository.findActiveTariffs(
                request.getOriginCountry(),
                request.getDestinationCountry(),
                request.getProduct(),
                validFrom,
                validTo
        );

        if (!overlapping.isEmpty()) {
            if (!request.isAllowOverride()) {
                throw new IllegalArgumentException("Overlapping validity range for this route/HS code. " +
                        "Adjust the dates or enable override to close the existing range.");
            }
            LocalDate cutoff = validFrom.minusDays(1);
            for (Tariff existing : overlapping) {
                if (cutoff.isBefore(existing.getValidFrom())) {
                    tariffRepository.delete(existing);
                } else {
                    existing.setValidTo(cutoff);
                    tariffRepository.save(existing);
                }
            }
        }

        Tariff tariff = Tariff.builder()
                .product(request.getProduct())
                .originCountry(request.getOriginCountry())
                .destinationCountry(request.getDestinationCountry())
                .rate(request.getRate())
                .validFrom(validFrom)
                .validTo(validTo)
                .label(StringUtils.hasText(request.getLabel()) ? request.getLabel().trim() : "Custom tariff")
                .notes(StringUtils.hasText(request.getNotes()) ? request.getNotes().trim() : null)
                .createdBy(createdBy)
                .build();

        return tariffRepository.save(tariff);
    }

    public void deleteTariff(long id) {
        if (!tariffRepository.existsById(id)) {
            throw new IllegalArgumentException("Tariff not found: " + id);
        }
        tariffRepository.deleteById(id);
    }

    private void validateRequest(AdminTariffRequest request) {
        if (request.getValidFrom().isAfter(request.getValidTo())) {
            throw new IllegalArgumentException("validFrom must be on or before validTo.");
        }
        if (request.getRate() < 0 || request.getRate() > 1) {
            throw new IllegalArgumentException("Rate must be between 0 and 1 (decimal form).");
        }
    }
}
