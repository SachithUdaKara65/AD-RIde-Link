package com.ridelink.drivervehicle.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateVehicleRequest {

    @NotBlank(message = "Make is required")
    private String make;

    @NotBlank(message = "Model is required")
    private String model;

    @NotNull(message = "Year is required")
    private Integer year;

    private String color;

    @NotBlank(message = "License plate is required")
    private String licensePlate;

    private String vehicleType; // CAR, VAN, MOTORCYCLE
}