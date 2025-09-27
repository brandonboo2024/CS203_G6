package com.example.tariffkey.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.Map;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/api/tariff")
@CrossOrigin(origins = "http://localhost:5173")
public class TariffHistoryController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @PostMapping("/history")
    public List<Map<String, Object>> getTariffHistory(@RequestBody TariffHistoryRequest request) {
        String sql = """
            SELECT product_code, origin_country, dest_country, rate_percent, valid_from, valid_to
            FROM (
                SELECT product_code, NULL as origin_country, NULL as dest_country, 
                       rate_percent, valid_from, valid_to
                FROM product_tariff_default_hist
                WHERE valid_from <= ? AND (valid_to IS NULL OR valid_to > ?)
                
                UNION ALL
                
                SELECT product_code, origin_country, dest_country, 
                       rate_percent, valid_from, valid_to
                FROM route_tariff_override_hist
                WHERE valid_from <= ? AND (valid_to IS NULL OR valid_to > ?)
            ) AS combined
            WHERE (? = '' OR product_code ILIKE ?)
            ORDER BY valid_from
        """;

        // Convert string dates to LocalDate
        LocalDate startDate = LocalDate.parse(request.getStartDate(), formatter);
        LocalDate endDate = LocalDate.parse(request.getEndDate(), formatter);

        String productCodeFilter = request.getProductCode() != null ? request.getProductCode() : "";
        String productCodeLike = "%" + productCodeFilter + "%";

        return jdbcTemplate.queryForList(sql, 
            endDate, startDate,
            endDate, startDate,
            productCodeFilter, productCodeLike);
    }

    public static class TariffHistoryRequest {
        private String productCode;
        private String startDate;
        private String endDate;

        // Getters and setters
        public String getProductCode() { return productCode; }
        public void setProductCode(String productCode) { this.productCode = productCode; }

        public String getStartDate() { return startDate; }
        public void setStartDate(String startDate) { this.startDate = startDate; }

        public String getEndDate() { return endDate; }
        public void setEndDate(String endDate) { this.endDate = endDate; }
    }
}
