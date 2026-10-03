package com.ridelink.account.dto;

public record DriverProfileRequest(
        String accountId,
        String licenseNumber,
        String serviceArea) {
}
