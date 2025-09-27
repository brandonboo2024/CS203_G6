package com.example.tariffkey.repository;

import com.example.tariffkey.model.PastCalculations;
import com.example.tariffkey.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PastCalculationsRepository extends JpaRepository<PastCalculations, Long> {
    // Find all calculations for a specific user
    List<PastCalculations> findByUser(User user);
    
    // Find all calculations for a user, ordered by calculation time (newest first)
    List<PastCalculations> findByUserOrderByCalculationTimeDesc(User user);
    
    // Find calculations within a date range for a user
    List<PastCalculations> findByUserAndCalculationTimeBetween(
        User user, 
        LocalDateTime startDate, 
        LocalDateTime endDate
    );
    
    // Count how many calculations a user has
    long countByUser(User user);
}