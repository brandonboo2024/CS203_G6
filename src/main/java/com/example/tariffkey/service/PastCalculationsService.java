package com.example.tariffkey.service;

import com.example.tariffkey.model.PastCalculations;
import com.example.tariffkey.model.TariffResponse;
import com.example.tariffkey.model.User;
import com.example.tariffkey.repository.PastCalculationsRepository;
import com.example.tariffkey.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class PastCalculationsService {
    private final PastCalculationsRepository pastCalculationsRepository;
    private final UserRepository userRepository;

    public PastCalculationsService(PastCalculationsRepository pastCalculationsRepository, UserRepository userRepository) {
        this.pastCalculationsRepository = pastCalculationsRepository;
        this.userRepository = userRepository;
    }

    /**
     * Save a new calculation from a TariffResponse for a specific user
     */
    public PastCalculations saveCalculation(TariffResponse response, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));

        // Create new calculation
        PastCalculations calculation = PastCalculations.builder()
                .user(user)
                .itemPrice(response.getItemPrice())
                .tariffRate(response.getTariffRate())
                .tariffAmount(response.getTariffAmount())
                .handlingFee(response.getHandlingFee())
                .inspectionFee(response.getInspectionFee())
                .processingFee(response.getProcessingFee())
                .otherFees(response.getOtherFees())
                .totalPrice(response.getTotalPrice())
                .calculationTime(LocalDateTime.now())
                .segments(response.getSegments().stream()
                    .map(this::convertToSegment)
                    .toList())
                .build();

        return pastCalculationsRepository.save(calculation);
    }

    /**
     * Get all calculations for a user, ordered by newest first
     */
    public List<PastCalculations> getUserCalculationHistory(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));
        return pastCalculationsRepository.findByUserOrderByCalculationTimeDesc(user);
    }

    /**
     * Get recent calculations (within specified days) for a user
     */
    public List<PastCalculations> getRecentCalculations(String username, int days) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));
        
        LocalDateTime startDate = LocalDateTime.now().minusDays(days);
        return pastCalculationsRepository.findByUserAndCalculationTimeBetween(
                user, 
                startDate, 
                LocalDateTime.now()
        );
    }

    /**
     * Delete a specific calculation by ID (only if it belongs to the specified user)
     */
    public void deleteCalculation(Long calculationId, String username) {
        PastCalculations calculation = pastCalculationsRepository.findById(calculationId)
                .orElseThrow(() -> new RuntimeException("Calculation not found"));
        
        // Security check: ensure the calculation belongs to the user
        if (!calculation.getUser().getUsername().equals(username)) {
            throw new RuntimeException("Not authorized to delete this calculation");
        }
        
        pastCalculationsRepository.delete(calculation);
    }

    /**
     * Get the total number of calculations for a user
     */
    public long getUserCalculationCount(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));
        return pastCalculationsRepository.countByUser(user);
    }

    /**
     * Helper method to convert TariffResponse.Segment to PastCalculations.PastCalculationSegment
     */
    private PastCalculations.PastCalculationSegment convertToSegment(TariffResponse.Segment responseSegment) {
        PastCalculations.PastCalculationSegment segment = new PastCalculations.PastCalculationSegment();
        segment.setFrom(responseSegment.getFrom());
        segment.setTo(responseSegment.getTo());
        segment.setRatePercent(responseSegment.getRatePercent());
        segment.setQuantityPortion(responseSegment.getQuantityPortion());
        segment.setItemPrice(responseSegment.getItemPrice());
        segment.setTariffAmount(responseSegment.getTariffAmount());
        segment.setLabel(responseSegment.getLabel());
        segment.setSource(responseSegment.getSource());
        return segment;
    }
}