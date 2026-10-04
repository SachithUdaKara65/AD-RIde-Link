package com.ridelink.drivervehicle.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class DriverProfileRequest {

    @NotBlank(message = "License number is required")
    private String licenseNumber;

    private String serviceArea;
}
