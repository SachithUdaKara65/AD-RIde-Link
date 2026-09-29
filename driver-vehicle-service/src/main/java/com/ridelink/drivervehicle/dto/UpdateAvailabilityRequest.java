package com.ridelink.drivervehicle.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateAvailabilityRequest {

    @NotBlank(message = "Availability is required")
    private String availability; // AVAILABLE, BUSY, OFFLINE
}