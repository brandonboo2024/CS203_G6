package com.example.tariffkey.controller;

import com.example.tariffkey.model.LookupOption;
import com.example.tariffkey.model.LookupResponse;
import com.example.tariffkey.security.JwtAuthenticationFilter;
import com.example.tariffkey.service.LookupService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(LookupController.class)
@AutoConfigureMockMvc(addFilters = false)
class LookupControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private LookupService lookupService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    void getLookupsReturnsReporterOptions() throws Exception {
        LookupResponse response = new LookupResponse(List.of(
            new LookupOption("USA", "United States"),
            new LookupOption("SGP", "Singapore")
        ));
        when(lookupService.getReporters()).thenReturn(response);

        mockMvc.perform(get("/api/lookups").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.reporters[0].code").value("USA"))
            .andExpect(jsonPath("$.reporters[1].label").value("Singapore"));

        verify(lookupService).getReporters();
    }

    @Test
    void getPartnersReturnsLookupOptions() throws Exception {
        List<LookupOption> partners = List.of(
            new LookupOption("CHN", "China"),
            new LookupOption("SGP", "Singapore")
        );
        when(lookupService.getPartnersForReporter(anyString())).thenReturn(partners);

        mockMvc.perform(get("/api/lookups/reporters/{reporter}/partners", "USA"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].code").value("CHN"))
            .andExpect(jsonPath("$[1].label").value("Singapore"));

        verify(lookupService).getPartnersForReporter("USA");
    }

    @Test
    void getProductsReturnsLookupOptions() throws Exception {
        List<LookupOption> products = List.of(
            new LookupOption("P001", "Widgets", "1234", true),
            new LookupOption("1234", "Fallback", "1234", false)
        );
        when(lookupService.getProductsForRoute(anyString(), anyString())).thenReturn(products);

        mockMvc.perform(get("/api/lookups/reporters/{reporter}/partners/{partner}/products", "USA", "SGP"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].code").value("P001"))
            .andExpect(jsonPath("$[1].priceAvailable").value(false));

        verify(lookupService).getProductsForRoute("USA", "SGP");
    }
}
