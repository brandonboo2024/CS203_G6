package com.example.tariffkey.model;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "wits_product_metadata")
public class WitsProductMetadata {

    @EmbeddedId
    private WitsProductMetadataId id;

    @Column(name = "tier")
    private Integer tier;

    @Column(name = "description")
    private String description;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;
}
