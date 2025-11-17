package com.example.tariffkey.controller;

import com.example.tariffkey.model.AdminTariffRequest;
import com.example.tariffkey.model.Tariff;
import com.example.tariffkey.model.TariffApiRequest;
import com.example.tariffkey.model.TariffApiResponse;
import com.example.tariffkey.model.TariffHistoryRequest;
import com.example.tariffkey.model.TariffHistoryResponse;
import com.example.tariffkey.model.TariffRequest;
import com.example.tariffkey.model.TariffResponse;
import com.example.tariffkey.security.JwtAuthenticationFilter;
import com.example.tariffkey.service.DefaultQuoteService;
import com.example.tariffkey.service.TariffHistoryService;
import com.example.tariffkey.service.TariffManagementService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TariffController.class)
@AutoConfigureMockMvc(addFilters = false)
class TariffControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private DefaultQuoteService quoteService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean
    private TariffManagementService tariffManagementService;

    @MockBean
    private TariffHistoryService tariffHistoryService;

    @Test
    void calculateReturnsQuoteFromService() throws Exception {
        TariffResponse response = new TariffResponse();
        response.setTariffRate(5.0);
        response.setMessage("ok");
        when(quoteService.calculateQuote(any(TariffRequest.class))).thenReturn(response);

        TariffRequest request = new TariffRequest();
        request.setFromCountry("USA");
        request.setToCountry("CHN");
        request.setHsCode("123456");
        request.setQuantity(10);
        request.setCustomBasePrice(50.0);

        mockMvc.perform(post("/api/tariff/calculate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.tariffRate").value(5.0))
            .andExpect(jsonPath("$.message").value("ok"));

        verify(quoteService).calculateQuote(any(TariffRequest.class));
    }

    @Test
    void quoteEndpointReturnsApiResponse() throws Exception {
        TariffApiResponse apiResponse = new TariffApiResponse();
        apiResponse.setTariffRate(0.12);
        apiResponse.setLabel("dataset");
        when(quoteService.fetchQuote(any(TariffApiRequest.class))).thenReturn(apiResponse);

        TariffApiRequest request = new TariffApiRequest();
        request.setOriginCountry("USA");
        request.setDestCountry("CAN");
        request.setHs6("010101");

        mockMvc.perform(post("/api/tariff/quote")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.tariffRate").value(0.12))
            .andExpect(jsonPath("$.label").value("dataset"));

        verify(quoteService).fetchQuote(any(TariffApiRequest.class));
    }

    @Test
    void addTariffUsesAuthenticatedUserName() throws Exception {
        Tariff saved = Tariff.builder()
            .id(42L)
            .product("847130")
            .originCountry("USA")
            .destinationCountry("SGP")
            .rate(0.05)
            .label("Saved")
            .validFrom(LocalDate.now())
            .validTo(LocalDate.now().plusDays(1))
            .build();
        when(tariffManagementService.addTariff(any(AdminTariffRequest.class), any()))
            .thenReturn(saved);

        AdminTariffRequest request = new AdminTariffRequest();
        request.setProduct("847130");
        request.setOriginCountry("USA");
        request.setDestinationCountry("SGP");
        request.setRate(0.05);
        request.setValidFrom(LocalDate.of(2024, 1, 1));
        request.setValidTo(LocalDate.of(2024, 12, 31));
        request.setLabel("New");

        mockMvc.perform(post("/api/tariff/add")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(42))
            .andExpect(jsonPath("$.label").value("Saved"));

        verify(tariffManagementService).addTariff(any(AdminTariffRequest.class), any());
    }

    @Test
    void getHistoryDelegatesToService() throws Exception {
        TariffHistoryResponse response = TariffHistoryResponse.builder()
            .summary(null)
            .data(List.of())
            .build();
        when(tariffHistoryService.getHistory(any(TariffHistoryRequest.class))).thenReturn(response);

        TariffHistoryRequest request = new TariffHistoryRequest();
        request.setProductCode("847130");
        request.setOriginCountry("USA");
        request.setDestCountry("SGP");

        mockMvc.perform(post("/api/tariff/history")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk());

        verify(tariffHistoryService).getHistory(any(TariffHistoryRequest.class));
    }

    @Test
    void deleteTariffInvokesService() throws Exception {
        mockMvc.perform(delete("/api/tariff/{id}", 99L))
            .andExpect(status().isOk());

        verify(tariffManagementService).deleteTariff(99L);
    }

    @Test
    void testEndpointReturnsHealthMessage() throws Exception {
        mockMvc.perform(get("/api/tariff"))
            .andExpect(status().isOk())
            .andExpect(content().string("TariffController active"));
    }
}
