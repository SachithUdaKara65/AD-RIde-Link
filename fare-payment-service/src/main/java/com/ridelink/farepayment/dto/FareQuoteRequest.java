package com.ridelink.farepayment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class FareQuoteRequest {

    @NotBlank(message = "Ride ID is required")
    private String rideId;

    @NotBlank(message = "Passenger ID is required")
    private String passengerId;

    @NotBlank(message = "Driver ID is required")
    private String driverId;

    @NotNull(message = "Distance is required")
    @Positive(message = "Distance must be greater than zero")
    private Double distanceKm;

    @NotNull(message = "Duration is required")
    @Positive(message = "Duration must be greater than zero")
    private Integer durationMinutes;

    @NotBlank(message = "Service type is required")
    private String serviceType = "STANDARD";
}
