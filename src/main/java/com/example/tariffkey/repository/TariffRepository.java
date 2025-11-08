package com.example.tariffkey.repository;

import com.example.tariffkey.model.Tariff;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TariffRepository extends JpaRepository<Tariff, Long> {

    @Query("""
            SELECT t FROM Tariff t
            WHERE t.originCountry = :origin
              AND t.destinationCountry = :destination
              AND t.product = :product
              AND t.validFrom <= :windowEnd
              AND t.validTo >= :windowStart
            ORDER BY t.validFrom DESC
            """)
    List<Tariff> findActiveTariffs(
            @Param("origin") String originCountry,
            @Param("destination") String destinationCountry,
            @Param("product") String product,
            @Param("windowStart") LocalDate windowStart,
            @Param("windowEnd") LocalDate windowEnd);

    Optional<Tariff> findTopByOriginCountryAndDestinationCountryAndProductOrderByValidFromDesc(
            String originCountry,
            String destinationCountry,
            String product);
}
