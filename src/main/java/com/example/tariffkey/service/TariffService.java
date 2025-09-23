package com.example.tariffkey.service;

import com.example.tariffkey.entity.*;
import com.example.tariffkey.repository.*;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class TariffService {

    private final ProductTariffDefaultRepository productTariffDefaultRepository;
    private final RouteTariffOverrideRepository routeTariffOverrideRepository;
    private final FeeScheduleRepository feeScheduleRepository;

    public TariffService(ProductTariffDefaultRepository productTariffDefaultRepository,
                         RouteTariffOverrideRepository routeTariffOverrideRepository,
                         FeeScheduleRepository feeScheduleRepository) {
        this.productTariffDefaultRepository = productTariffDefaultRepository;
        this.routeTariffOverrideRepository = routeTariffOverrideRepository;
        this.feeScheduleRepository = feeScheduleRepository;
    }

    public double calculateTariff(String productCode, String originCountry, String destCountry, double basePrice) {
        double ratePercent = routeTariffOverrideRepository
            .findByProductCodeAndOriginCountryAndDestCountry(productCode, originCountry, destCountry)
            .map(RouteTariffOverride::getRatePercent)
            .orElseGet(() -> productTariffDefaultRepository.findById(productCode)
                .map(ProductTariffDefault::getRatePercent)
                .orElse(0.0)
            );

        return basePrice * ratePercent / 100.0;
    }

    public List<FeeSchedule> getAllFees() {
        return feeScheduleRepository.findAll();
    }
}
