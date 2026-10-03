package com.ridelink.drivervehicle.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class DriverResponse {

    private String id;
    private String accountId;
    private String licenseNumber;
    private String status;
    private String availability;
    private String serviceArea;
    private Double currentLatitude;
    private Double currentLongitude;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}