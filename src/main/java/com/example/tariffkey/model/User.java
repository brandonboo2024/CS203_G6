package com.example.tariffkey.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "users")  
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String passwordHash;

    @Builder.Default
    private String role = "USER";   // default role
    
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<PastCalculations> pastCalculations = new ArrayList<>();

    // Helper method to add past calculation
    public void addPastCalculation(PastCalculations calculation) {
        pastCalculations.add(calculation);
        calculation.setUser(this);
    }

    // Helper method to remove past calculation
    public void removePastCalculation(PastCalculations calculation) {
        pastCalculations.remove(calculation);
        calculation.setUser(null);
    }
}
