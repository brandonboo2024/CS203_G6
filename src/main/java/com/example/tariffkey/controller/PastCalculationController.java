package com.example.tariffkey.controller;

import com.example.tariffkey.model.PastCalculations;
import com.example.tariffkey.model.TariffResponse;
import com.example.tariffkey.model.User;
import com.example.tariffkey.service.PastCalculationsService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/calculations")
@CrossOrigin(origins = "http://localhost:5173")
public class PastCalculationController {
    private final PastCalculationsService pastCalculationsService;

    public PastCalculationController(PastCalculationsService pastCalculationsService) {
        this.pastCalculationsService = pastCalculationsService;
    }

    @PostMapping("/save")
    public ResponseEntity<PastCalculations> savePastCalculation(
            @AuthenticationPrincipal User user,
            @RequestBody TariffResponse tariffResponse) {
        PastCalculations saved = pastCalculationsService.saveCalculation(tariffResponse, user.getUsername());
        return ResponseEntity.ok(saved);
    }

    @GetMapping("/history")
    public ResponseEntity<List<PastCalculations>> getUserHistory(@AuthenticationPrincipal User user) {
        List<PastCalculations> history = pastCalculationsService.getUserCalculationHistory(user.getUsername());
        return ResponseEntity.ok(history);
    }

    @GetMapping("/recent")
    public ResponseEntity<List<PastCalculations>> getRecentCalculations(
            @AuthenticationPrincipal User user,
            @RequestParam(defaultValue = "30") int days) {
        List<PastCalculations> recent = pastCalculationsService.getRecentCalculations(user.getUsername(), days);
        return ResponseEntity.ok(recent);
    }

}
