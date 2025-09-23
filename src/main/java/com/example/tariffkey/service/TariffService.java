package com.example.tariffkey.service;

import com.example.tariffkey.entity.FeeSchedule;
import com.example.tariffkey.entity.FeeType;
import com.example.tariffkey.entity.ProductTariffDefault;
import com.example.tariffkey.entity.RouteTariffOverride;
import com.example.tariffkey.repository.FeeScheduleRepository;
import com.example.tariffkey.repository.ProductTariffDefaultRepository;
import com.example.tariffkey.repository.RouteTariffOverrideRepository;
import org.springframework.stereotype.Service;
import java.util.Optional;
import java.util.List;


@Service
public class TariffService {

    private final ProductTariffDefaultRepository productTariffRepo;
    private final RouteTariffOverrideRepository routeTariffRepo;
    private final FeeScheduleRepository feeRepo;

    public TariffService(ProductTariffDefaultRepository productTariffRepo,
                         RouteTariffOverrideRepository routeTariffRepo,
                         FeeScheduleRepository feeRepo) {
        this.productTariffRepo = productTariffRepo;
        this.routeTariffRepo = routeTariffRepo;
        this.feeRepo = feeRepo;
    }

    public double calculateTariff(String productCode, String origin, String dest, double basePrice) {
        RouteTariffOverride override = routeTariffRepo.findByProductCodeAndOriginCountryAndDestCountry(
                productCode, origin, dest
        ).orElse(null);

        double ratePercent;

        if (override != null) {
            ratePercent = override.getRatePercent();
        } else {
            ProductTariffDefault def = productTariffRepo.findById(productCode).orElse(null);
            ratePercent = def != null ? def.getRatePercent() : 0.0;
        }

        return basePrice * ratePercent / 100.0;
    }

    public double calculateFees(List<String> feeNames) {
        if (feeNames == null || feeNames.isEmpty()) return 0.0;

        return feeNames.stream()
        .map(name -> {
            try {
                return FeeType.valueOf(name.toUpperCase()); 
            } catch (IllegalArgumentException e) {
                return null; 
            }
        })
        .filter(feeType -> feeType != null)
        .map(feeRepo::findById)       // now finds by FeeType
        .filter(Optional::isPresent)
        .mapToDouble(opt -> opt.get().getAmount())
        .sum();
    }

    public List<FeeSchedule> getAllFees() {
        return feeRepo.findAll();
    }
}
