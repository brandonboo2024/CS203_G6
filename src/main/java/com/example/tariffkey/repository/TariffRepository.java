package com.example.tariffkey.repository;

import com.example.tariffkey.model.Tariff;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TariffRepository extends JpaRepository<Tariff, Long> {
    Optional<Tariff> findByOriginCountryAndDestinationCountryAndProduct(String originCountry, String destinationCountry, String product);
}
