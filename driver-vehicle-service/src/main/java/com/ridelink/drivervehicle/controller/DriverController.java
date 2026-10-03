package com.ridelink.drivervehicle.controller;

import com.ridelink.drivervehicle.dto.*;
import com.ridelink.drivervehicle.model.Vehicle;
import com.ridelink.drivervehicle.service.DriverService;
import io.swagger.v3.oas.annotations.Hidden;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;

@RestController
@RequestMapping("/api/drivers")
@RequiredArgsConstructor
public class DriverController {

    private final DriverService driverService;

    @Value("${driver-service.service-token:}")
    private String serviceToken;

    @Hidden
    @PostMapping("/internal")
    public ResponseEntity<DriverResponse> createDriver(
            @RequestHeader(value = "X-Service-Token", required = false) String providedToken,
            @Valid @RequestBody CreateDriverRequest request) {
        validateServiceToken(providedToken);
        DriverResponse response = driverService.createDriver(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    private void validateServiceToken(String providedToken) {
        if (serviceToken.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE, "Driver service authentication is not configured.");
        }
        if (providedToken == null || !MessageDigest.isEqual(
                serviceToken.getBytes(StandardCharsets.UTF_8),
                providedToken.getBytes(StandardCharsets.UTF_8))) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid service credentials.");
        }
    }

    // Get Driver by ID
    @GetMapping("/{id}")
    @io.swagger.v3.oas.annotations.Operation(
            summary = "Get driver by profile ID or Account Service user ID")
    @PreAuthorize("hasAnyRole('DRIVER', 'ADMIN', 'PASSENGER')")
    public ResponseEntity<DriverResponse> getDriverById(@PathVariable String id) {
        return ResponseEntity.ok(driverService.getDriverById(id));
    }

    // Update Availability - only DRIVER or ADMIN
    @PatchMapping("/{id}/availability")
    @PreAuthorize("hasAnyRole('DRIVER', 'ADMIN')")
    public ResponseEntity<DriverResponse> updateAvailability(
            @PathVariable String id,
            @Valid @RequestBody UpdateAvailabilityRequest request) {
        return ResponseEntity.ok(driverService.updateAvailability(id, request));
    }

    // Update Location - only DRIVER or ADMIN
    @PatchMapping("/{id}/location")
    @PreAuthorize("hasAnyRole('DRIVER', 'ADMIN')")
    public ResponseEntity<DriverResponse> updateLocation(
            @PathVariable String id,
            @Valid @RequestBody UpdateLocationRequest request) {
        return ResponseEntity.ok(driverService.updateLocation(id, request));
    }

    // Get Available Drivers - PUBLIC (no token needed)
    // This allows Ride Management Service to call it easily
    @GetMapping("/available")
    public ResponseEntity<List<DriverResponse>> getAvailableDrivers(
            @RequestParam(required = false) String serviceArea) {
        return ResponseEntity.ok(driverService.getAvailableDrivers(serviceArea));
    }

    // Add Vehicle - only DRIVER or ADMIN
    @PostMapping("/{driverId}/vehicles")
    @PreAuthorize("hasAnyRole('DRIVER', 'ADMIN')")
    public ResponseEntity<Vehicle> addVehicle(
            @PathVariable String driverId,
            @Valid @RequestBody CreateVehicleRequest request) {
        Vehicle vehicle = driverService.addVehicle(driverId, request);
        return new ResponseEntity<>(vehicle, HttpStatus.CREATED);
    }

    // Get Vehicles of a Driver
    @GetMapping("/{driverId}/vehicles")
    @PreAuthorize("hasAnyRole('DRIVER', 'ADMIN')")
    public ResponseEntity<List<Vehicle>> getVehicles(@PathVariable String driverId) {
        return ResponseEntity.ok(driverService.getVehiclesByDriver(driverId));
    }
}