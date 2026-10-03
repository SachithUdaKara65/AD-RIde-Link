package com.ridelink.farepayment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FareQuoteResponse {
    private String quoteId;
    private String rideId;
    private String passengerId;
    private String driverId;
    private double distanceKm;
    private int durationMinutes;
    private String serviceType;
    private BigDecimal baseFare;
    private BigDecimal distanceFare;
    private BigDecimal timeFare;
    private BigDecimal surgeMultiplier;
    private BigDecimal totalFare;
    private String currency;
    private LocalDateTime createdAt;
}
