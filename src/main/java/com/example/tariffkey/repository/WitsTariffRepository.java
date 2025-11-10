package com.example.tariffkey.repository;

import com.example.tariffkey.model.WitsTariff;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface WitsTariffRepository extends JpaRepository<WitsTariff, Long> {

    Optional<WitsTariff> findFirstByReporterIsoAndPartnerCodeAndProductCodeAndYearOrderByIdAsc(
            String reporterIso,
            String partnerCode,
            String productCode,
            Integer year);

    Optional<WitsTariff> findFirstByReporterIsoAndPartnerCodeAndProductCodeOrderByYearDesc(
            String reporterIso,
            String partnerCode,
            String productCode);

    boolean existsByReporterIso(String reporterIso);

    @Query("""
            select w.reporterIso as reporterIso,
                   min(w.sourceFile) as sourceFile
            from WitsTariff w
            group by w.reporterIso
            order by w.reporterIso asc
            """)
    List<ReporterSample> findReporterSamples();

    @Query("""
            select distinct w.partnerCode
            from WitsTariff w
            where w.reporterIso = :reporter
            order by w.partnerCode asc
            """)
    List<String> findPartnersByReporter(@Param("reporter") String reporter);

    @Query("""
            select distinct w.productCode as productCode,
                            w.nomenCode as nomenCode
            from WitsTariff w
            where w.reporterIso = :reporter and w.partnerCode = :partner
            order by w.productCode asc
            """)
    List<ProductSample> findProductSamplesByRoute(@Param("reporter") String reporter,
                                                  @Param("partner") String partner);

    @Query("""
            select w
            from WitsTariff w
            where (:productCode is null or w.productCode = :productCode)
              and (:origin is null or w.reporterIso = :origin)
              and (:dest is null or w.partnerCode = :dest)
              and w.year between :startYear and :endYear
            """)
    Page<WitsTariff> findHistory(@Param("productCode") String productCode,
                                 @Param("origin") String origin,
                                 @Param("dest") String dest,
                                 @Param("startYear") Integer startYear,
                                 @Param("endYear") Integer endYear,
                                 Pageable pageable);

    interface ReporterSample {
        String getReporterIso();
        String getSourceFile();
    }

    interface ProductSample {
        String getProductCode();
        String getNomenCode();
    }
}
