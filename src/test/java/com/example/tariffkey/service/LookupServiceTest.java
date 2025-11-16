package com.example.tariffkey.service;



import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.apache.naming.LookupRef;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.tariffkey.repository.ProductRepository;
import com.example.tariffkey.repository.WitsCountryMetadataRepository;
import com.example.tariffkey.repository.WitsProductMetadataRepository;
import com.example.tariffkey.repository.WitsTariffRepository;
import com.example.tariffkey.model.LookupOption;
import com.example.tariffkey.model.LookupResponse;
import com.example.tariffkey.model.Product;
import com.example.tariffkey.model.WitsCountryMetadata;
import com.example.tariffkey.model.WitsProductMetadata;
import com.example.tariffkey.model.WitsProductMetadataId;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.tariffkey.model.WitsTariff;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.tariffkey.model.LookupResponse;


@ExtendWith(MockitoExtension.class)
public class LookupServiceTest {
    
    @Mock
    private WitsTariffRepository witsTariffRepository;
    @Mock
    private WitsCountryMetadataRepository countryMetadataRepository;
    @Mock
    private WitsProductMetadataRepository productMetadataRepository;
    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private LookupService lookupServ;

    @Test
    @DisplayName("Should convert samples to LookupOptions object and sort them by Label")
    void getReporters_ShouldReturnLookupOptionsWhichAreSorted_WhenCalledAndProvidedWithMultipleSamples(){
        //Arrange
        //Mock input samples
        WitsTariffRepository.ReporterSample sampleA = mock(WitsTariffRepository.ReporterSample.class);
        when(sampleA.getReporterIso()).thenReturn("SgCode");
        when(sampleA.getSourceFile()).thenReturn("dummy.zip");


        WitsTariffRepository.ReporterSample sampleB = mock(WitsTariffRepository.ReporterSample.class);
        when(sampleB.getReporterIso()).thenReturn("MyCode");
        when(sampleB.getSourceFile()).thenReturn("dummy2.zip");

        when(witsTariffRepository.findReporterSamples()).thenReturn(List.of(sampleA, sampleB));

        //Mock metadata Lookups
        WitsCountryMetadata SgMeta = new WitsCountryMetadata();
        SgMeta.setCountryName("Singapore");
        SgMeta.setIso3("SGP");
        SgMeta.setCountryCode("SgCode");

        WitsCountryMetadata MyMeta = new WitsCountryMetadata();
        MyMeta.setCountryName("Malaysia");
        MyMeta.setIso3("MYS");
        MyMeta.setCountryCode("MyCode");


        when(countryMetadataRepository.findById("SgCode")).thenReturn(Optional.of(SgMeta));
        when(countryMetadataRepository.findById("MyCode")).thenReturn(Optional.of(MyMeta));

        //Act
        LookupResponse response = lookupServ.getReporters();

        //Assert
        assertNotNull(response);
        assertEquals(2 , response.reporters().size());

        //They should be sorted alphabetically , Malaysia First then Singapore
        LookupOption first = response.reporters().get(0);
        LookupOption second = response.reporters().get(1);

        assertEquals("MyCode", first.code());
        assertEquals("SgCode", second.code());

        assertEquals("Malaysia (MYS · MyCode)", first.label());
        assertEquals("Singapore (SGP · SgCode)", second.label());

        verify(witsTariffRepository , times(1)).findReporterSamples();
        verify(countryMetadataRepository, times(1)).findById("SgCode");
        verify(countryMetadataRepository, times(1)).findById("MyCode");
    }


    @Test
    @DisplayName("Should call fallBackReporterLabel and fallback to sourceBased Label when metadata not returned and missing")
    void getReporters_ShouldFallBackToSourceBasedLabel_WhenMetadataMissing(){
        //Arrange
        WitsTariffRepository.ReporterSample sample = mock(WitsTariffRepository.ReporterSample.class);

        when(sample.getReporterIso()).thenReturn("ZZ");
        when(sample.getSourceFile()).thenReturn("TRADE_2024_usa_ZZ,zip");
        when(witsTariffRepository.findReporterSamples()).thenReturn(List.of(sample));

        //return no metadata, force the fallBackReporterLabel
        when(countryMetadataRepository.findById("ZZ")).thenReturn(Optional.empty());

        //Act 
        LookupResponse response = lookupServ.getReporters();

        //Assert
        assertEquals(1, response.reporters().size());
        LookupOption first = response.reporters().get(0);

        assertEquals("ZZ", first.code());

        //Fallback format: CountryName (ISO3 · ReporterIso)
        assertEquals("United States (USA · ZZ)", first.label());
    }

    @Test
    @DisplayName("Should throw illegalArgumentException when reporterCode doesnt exist")
    void getPartnersForReporter_ShouldThrowIllegalArgumentException_WhenReporterCodeDontExist(){

        IllegalArgumentException e = assertThrows(IllegalArgumentException.class , ()-> lookupServ.getPartnersForReporter("non existent code"));
        assertNotNull(e);
        assertEquals("Unknown reporter code: non existent code", e.getMessage());
    }


    @Test
    @DisplayName("Should return List of PartnerCodes as List of LookupOptions when there are existing partners")
    void getPartnersForReporter_ShouldReturnListOfLookupOptions_WhenHavePartnersForReporter(){
        //Arrange
        String reporter = "SGCode";
        when(witsTariffRepository.existsByReporterIso(reporter)).thenReturn(true);
        when(witsTariffRepository.findPartnersByReporter(reporter)).thenReturn(List.of("CNCode", "MYCode"));

        //Mock metadata for the two countries listed
        WitsCountryMetadata CNMeta = new WitsCountryMetadata();
        CNMeta.setCountryCode("CNCode");
        CNMeta.setCountryName("China");
        CNMeta.setIso3("CHN");

        WitsCountryMetadata MYMeta = new WitsCountryMetadata();
        MYMeta.setCountryCode("MYCode");
        MYMeta.setCountryName("Malaysia");
        MYMeta.setIso3("MYS");

        when(countryMetadataRepository.findById("CNCode")).thenReturn(Optional.of(CNMeta));
        when(countryMetadataRepository.findById("MYCode")).thenReturn(Optional.of(MYMeta));

        //Act
        List<LookupOption> partnerCountries = lookupServ.getPartnersForReporter(reporter);

        //Assert
        assertEquals(2, partnerCountries.size());

        LookupOption first = partnerCountries.get(0);
        LookupOption second = partnerCountries.get(1);

        //Order should be based on alphabetical Order
        assertEquals("CNCode", first.code());
        assertEquals("China (CHN · CNCode)", first.label());

        assertEquals("MYCode", second.code());
        assertEquals("Malaysia (MYS · MYCode)", second.label());

        verify(witsTariffRepository, times(1)).existsByReporterIso(reporter);
        verify(witsTariffRepository, times(1)).findPartnersByReporter(reporter);

        verify(countryMetadataRepository, times(1)).findById("CNCode");
        verify(countryMetadataRepository, times(1)).findById("MYCode");
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when Reporter exist but no partner Exist")
    void getPartnersForReporter_ShouldThrowIllegalArgumentException_When_ReporterExistButPartnerDontExist(){
        //Arrange
        String reporter = "SGCode";
        when(witsTariffRepository.existsByReporterIso(reporter)).thenReturn(true);
        when(witsTariffRepository.findPartnersByReporter(reporter)).thenReturn(Collections.emptyList());

        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, ()-> lookupServ.getPartnersForReporter(reporter));

        assertEquals("No partners available for reporter SGCode", e.getMessage());
    }

    @Test
    @DisplayName("Should return fallback country code when no metadata")
    void getPartnersForReporter_ShouldReturnFallbackCountryCode_WhenNoMetadata(){ //covers the private CountryLabel Else branch
        //Arrange
        String reporter = "SGCode";

        when(witsTariffRepository.existsByReporterIso(reporter)).thenReturn(true);
        when(witsTariffRepository.findPartnersByReporter(reporter)).thenReturn(List.of("MYCode"));

        when(countryMetadataRepository.findById("MYCode")).thenReturn(Optional.empty());

        //Act
        List<LookupOption> partnerCodes = lookupServ.getPartnersForReporter(reporter);

        //Assert
        assertEquals(1, partnerCodes.size());
        assertEquals("MYCode", partnerCodes.get(0).code());
        assertEquals("MYCode", partnerCodes.get(0).label());
    }

    @Test
    @DisplayName("Should return IllegalArgumentException when reporter doesnt exist")
    void getProductsForRoute_ShouldReturnIllegalArgumentException_WhenReporterDoesntExist(){
        //Arrange + Act
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, ()-> lookupServ.getProductsForRoute("This is non existent code", "MYCode"));

        //Assert
        assertNotNull(e);
        assertEquals("Unknown reporter code: This is non existent code", e.getMessage());
    }

    @Test
    @DisplayName("Should return IllegalArgumentException when partner isnt provided")
    void getProductsForRoute_ShouldReturnIllegalArgumentException_WhenPartnerNotProvided(){
        //Arrange
        when(witsTariffRepository.existsByReporterIso("SGCode")).thenReturn(true);
        
        //Act
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> lookupServ.getProductsForRoute("SGCode", null));

        //Assert
        assertNotNull(e);
        assertEquals("Partner code is required", e.getMessage());
    }

    @Test
    @DisplayName("Should return IllegalArgumentException when there are no product routes between Reporter and Partner")
    void getProductsForRoute_ShouldReturnIllegalArgumentException_WhenNoValidRouteBetweenReporterAndPartner(){
        //Arrange
        when(witsTariffRepository.existsByReporterIso("SGCode")).thenReturn(true);
        when(witsTariffRepository.findProductSamplesByRoute("SGCode", "MYCode")).thenReturn(Collections.emptyList());

        //Act
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> lookupServ.getProductsForRoute("SGCode", "MYCode"));

        //Assert
        assertNotNull(e);
        assertEquals("No products available for the selected countries", e.getMessage());
    }

    @Test
    @DisplayName("Should return product LookupOptions object with metadata label when price exists")
    void getProductsForRoute_ShouldReturnProductsWithMetadataLabel_WhenPriceExists(){
        //Arrange
        when(witsTariffRepository.existsByReporterIso("SGCode")).thenReturn(true);

        //product samples return by witsTariff
        WitsTariffRepository.ProductSample sample = mock(WitsTariffRepository.ProductSample.class);
        when(sample.getProductCode()).thenReturn("1234");
        when(sample.getNomenCode()).thenReturn("HS");
        when(witsTariffRepository.findProductSamplesByRoute("SGCode", "MYCode")).thenReturn(List.of(sample));

        //Metadata for HS + Product Code
        WitsProductMetadata meta = new WitsProductMetadata();
        meta.setDescription("Widgets");
        when(productMetadataRepository.findById(any())).thenReturn(Optional.of(meta));

        //Price available (which means Product exists)
        Product priced = new Product();
        priced.setCode("P1234");
        priced.setHsCode("1234");
        when(productRepository.findByHsCodeIn(List.of("1234"))).thenReturn(List.of(priced));

        //Act
        List<LookupOption> result = lookupServ.getProductsForRoute("SGCode", "MYCode");

        //Assert
        assertEquals(1, result.size());

        LookupOption first = result.get(0);
        
        assertEquals("P1234", first.code()); //get Product Code
        assertEquals("1234", first.hsCode());// get HSCode
        assertTrue(first.priceAvailable());

        //label = "<description> (HS <code>)"
        assertEquals("Widgets (HS 1234)", first.label());

        verify(witsTariffRepository, times(1)).findProductSamplesByRoute("SGCode", "MYCode");
        verify(productMetadataRepository, times(1)).findById(any());
        verify(productRepository, times(1)).findByHsCodeIn(List.of("1234"));
    }

    @Test
    @DisplayName("Should append(price required) when product has no price")
    void getProductsForRoute_ShouldIndicateMissingPriceInLabel_WhenPriceMissing(){
        //arrange
        when(witsTariffRepository.existsByReporterIso("SGCode")).thenReturn(true);

        //ProductSample
        WitsTariffRepository.ProductSample sample = mock(WitsTariffRepository.ProductSample.class);
        when(sample.getProductCode()).thenReturn("9999");
        when(sample.getNomenCode()).thenReturn("HS");

        when(witsTariffRepository.findProductSamplesByRoute("SGCode", "MYCode")).thenReturn(List.of(sample));

        //Metadata Exists
        WitsProductMetadata meta = new WitsProductMetadata();
        meta.setDescription("Fancy Gadgets");
        when(productMetadataRepository.findById(any())).thenReturn(Optional.of(meta));

        //No pricing returned
        when(productRepository.findByHsCodeIn(List.of("9999"))).thenReturn(Collections.emptyList());

        //Act 
        List<LookupOption> result = lookupServ.getProductsForRoute("SGCode", "MYCode");

        //Assert
        assertEquals(1, result.size());

        LookupOption first = result.get(0);

        assertEquals("9999", first.code()); //falls back to HSCode
        assertEquals("9999", first.hsCode());
        assertFalse(first.priceAvailable());

        assertEquals("Fancy Gadgets (HS 9999) (price required)", first.label());
    }


    @Test
    @DisplayName("Should fallback to HSMetadata when precise metadata missing")
    void getProductsForRoute_ShouldUseHSMetadata_WhenPreciseMetadataMissing(){
        //Arrange
        when(witsTariffRepository.existsByReporterIso("SGCode")).thenReturn(true);

        WitsTariffRepository.ProductSample sample = mock(WitsTariffRepository.ProductSample.class);
        when(sample.getProductCode()).thenReturn("2222");
        when(sample.getNomenCode()).thenReturn("WCO");

        when(witsTariffRepository.findProductSamplesByRoute("SGCode", "MYCode")).thenReturn(List.of(sample));

        //missing metadata 
        when(productMetadataRepository.findById(any())).thenReturn(Optional.empty());

        //HS metadata exists
        WitsProductMetadata hsMeta = new WitsProductMetadata();
        hsMeta.setDescription("HS Desc");
        when(productMetadataRepository.findById(argThat(id -> id.getNomenCode().equals("HS"))))
        .thenReturn(Optional.of(hsMeta));

        //Price exists 
        Product p = new Product();
        p.setCode("C2222");
        p.setHsCode("2222");
        when(productRepository.findByHsCodeIn(any())).thenReturn(List.of(p));


        //Act
        List<LookupOption> result = lookupServ.getProductsForRoute("SGCode", "MYCode");

        //Assert
        LookupOption first = result.get(0);
        assertEquals(1, result.size());
        assertEquals("C2222", first.code());
        assertEquals("HS Desc (HS 2222)", first.label());
    }

    @Test
    @DisplayName("Should fallback to HSLabel when both precise metadata and hs metadata are missing")
    void getProductsForRoute_ShouldUseFallbackHsLabel_WhenAllMetadataMissing(){
        //arrange
        String reporter = "SGCode";
        String partner = "MYCode";
        String nomenCode = "WCO";
        String productCode = "7777";

        when(witsTariffRepository.existsByReporterIso(reporter)).thenReturn(true);

        //Route Produces one product sample
        WitsTariffRepository.ProductSample sample = mock(WitsTariffRepository.ProductSample.class);
        when(sample.getProductCode()).thenReturn(productCode);
        when(sample.getNomenCode()).thenReturn(nomenCode);

        when(witsTariffRepository.findProductSamplesByRoute(reporter, partner)).thenReturn(List.of(sample));

        //not priced product found in productRepository
        when(productRepository.findByHsCodeIn(List.of(productCode))).thenReturn(Collections.emptyList());

        //MetaData lookups all return empty
        when(productMetadataRepository.findById(new WitsProductMetadataId(nomenCode, productCode))).thenReturn(Optional.empty());
        when(productMetadataRepository.findById(new WitsProductMetadataId("HS", productCode))).thenReturn(Optional.empty());
        when(productMetadataRepository.findFirstByIdProductCode(productCode)).thenReturn(Optional.empty());

        //Act
        List<LookupOption> result = lookupServ.getProductsForRoute(reporter, partner);

        //Assert
        assertEquals(1, result.size());
        LookupOption first = result.get(0);

        assertEquals(productCode, first.code());
        assertEquals(productCode, first.hsCode());

        //Fallback description with price required suffix since product not in productRepository
        assertEquals("HS 7777 (price required)", first.label());

        assertFalse(first.priceAvailable());
    }
}
