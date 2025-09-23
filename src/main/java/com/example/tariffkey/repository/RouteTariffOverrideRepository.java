package com.example.tariffkey.repository;

import com.example.tariffkey.entity.RouteTariffOverride;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface RouteTariffOverrideRepository extends JpaRepository<RouteTariffOverride, Long> {
    
    Optional<RouteTariffOverride> findByProductCodeAndOriginCountryAndDestCountry(
            String productCode,
            String originCountry,
            String destCountry
    );
}
