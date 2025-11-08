package com.example.tariffkey.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class WitsProductMetadataId implements Serializable {

    @Column(name = "nomen_code", length = 10)
    private String nomenCode;

    @Column(name = "product_code", length = 20)
    private String productCode;
}
