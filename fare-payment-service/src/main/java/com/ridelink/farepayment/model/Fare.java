package com.ridelink.farepayment.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "fares")
public class Fare {

    @Id
    private String id;

    private String rideId;
    private String passengerId;
    private String driverId;
    private String serviceType;

    private double distanceKm;
    private int durationMinutes;

    @Builder.Default
    private BigDecimal baseFare = BigDecimal.ZERO;

    @Builder.Default
    private BigDecimal distanceFare = BigDecimal.ZERO;

    @Builder.Default
    private BigDecimal timeFare = BigDecimal.ZERO;

    @Builder.Default
    private BigDecimal surgeMultiplier = BigDecimal.ONE;

    @Builder.Default
    private BigDecimal totalFare = BigDecimal.ZERO;

    @Builder.Default
    private String currency = "LKR";

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
