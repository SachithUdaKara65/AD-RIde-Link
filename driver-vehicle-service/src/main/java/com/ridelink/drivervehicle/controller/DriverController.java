package com.ridelink.drivervehicle.controller;

import com.ridelink.drivervehicle.dto.*;
import com.ridelink.drivervehicle.model.Vehicle;
import com.ridelink.drivervehicle.service.DriverService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/drivers")
@RequiredArgsConstructor
public class DriverController {

    private final DriverService driverService;

    @PostMapping("/me/profile")
    @PreAuthorize("hasRole('DRIVER')")
    public ResponseEntity<DriverResponse> provisionDriverProfile(
            Authentication authentication,
            @Valid @RequestBody DriverProfileRequest profileRequest) {
        CreateDriverRequest request = new CreateDriverRequest();
        request.setAccountId(authentication.getName());
        request.setLicenseNumber(profileRequest.getLicenseNumber());
        request.setServiceArea(profileRequest.getServiceArea());
        DriverResponse response = driverService.createDriver(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/me/profile")
    @PreAuthorize("hasRole('DRIVER')")
    public ResponseEntity<DriverResponse> getMyDriverProfile(Authentication authentication) {
        return ResponseEntity.ok(driverService.getDriverByAccountId(authentication.getName()));
    }

    // Get Driver by ID
    @GetMapping("/{id}")
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