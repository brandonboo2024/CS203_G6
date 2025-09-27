package com.example.tariffkey.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "past_calculations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PastCalculations {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private double itemPrice;
    private double tariffRate;
    private double tariffAmount;
    private double handlingFee;
    private double inspectionFee;
    private double processingFee;
    private double otherFees;
    private Double totalPrice;
    
    @Column(name = "calculation_time")
    private LocalDateTime calculationTime;

    @ElementCollection
    @CollectionTable(
        name = "past_calculation_segments",
        joinColumns = @JoinColumn(name = "past_calculation_id")
    )
    private List<PastCalculationSegment> segments;

    // Embedded class for segments
    @Embeddable
    @Data
    public static class PastCalculationSegment {
        private String from;            // date time
        private String to;              // date time
        private double ratePercent;     // tariff in this segment
        private double quantityPortion; // quantity allocated to this segment
        private double itemPrice;       // portion * basePrice
        private double tariffAmount;    // itemPrice * rate
        private String label;
        private String source;
    }
}
