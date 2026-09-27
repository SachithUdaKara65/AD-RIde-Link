package com.ridelink.drivervehicle.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateDriverRequest {

    @NotBlank(message = "Account ID is required")
    private String accountId;

    @NotBlank(message = "Full name is required")
    private String fullName;

    @NotBlank(message = "Phone is required")
    private String phone;

    @NotBlank(message = "License number is required")
    private String licenseNumber;

    private String serviceArea;
}