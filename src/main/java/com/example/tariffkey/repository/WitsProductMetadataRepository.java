package com.example.tariffkey.repository;

import com.example.tariffkey.model.WitsProductMetadata;
import com.example.tariffkey.model.WitsProductMetadataId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WitsProductMetadataRepository extends JpaRepository<WitsProductMetadata, WitsProductMetadataId> {

    Optional<WitsProductMetadata> findFirstByIdProductCode(String productCode);
}
