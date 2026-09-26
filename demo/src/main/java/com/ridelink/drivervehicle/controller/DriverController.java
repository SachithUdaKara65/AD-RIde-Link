package com.ridelink.drivervehicle.controller;

import com.ridelink.drivervehicle.dto.*;
import com.ridelink.drivervehicle.model.Vehicle;
import com.ridelink.drivervehicle.service.DriverService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/drivers")
@RequiredArgsConstructor
public class DriverController {

    private final DriverService driverService;

    // 1. Create Driver Profile
    @PostMapping
    public ResponseEntity<DriverResponse> createDriver(@Valid @RequestBody CreateDriverRequest request) {
        DriverResponse response = driverService.createDriver(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    // 2. Get Driver by ID
    @GetMapping("/{id}")
    public ResponseEntity<DriverResponse> getDriverById(@PathVariable String id) {
        return ResponseEntity.ok(driverService.getDriverById(id));
    }

    // 3. Update Availability
    @PatchMapping("/{id}/availability")
    public ResponseEntity<DriverResponse> updateAvailability(
            @PathVariable String id,
            @Valid @RequestBody UpdateAvailabilityRequest request) {
        return ResponseEntity.ok(driverService.updateAvailability(id, request));
    }

    // 4. Update Location
    @PatchMapping("/{id}/location")
    public ResponseEntity<DriverResponse> updateLocation(
            @PathVariable String id,
            @Valid @RequestBody UpdateLocationRequest request) {
        return ResponseEntity.ok(driverService.updateLocation(id, request));
    }

    // 5. Get Available Drivers (Critical for Ride Management Service)
    @GetMapping("/available")
    public ResponseEntity<List<DriverResponse>> getAvailableDrivers(
            @RequestParam(required = false) String serviceArea) {
        return ResponseEntity.ok(driverService.getAvailableDrivers(serviceArea));
    }

    // 6. Add Vehicle to a Driver
    @PostMapping("/{driverId}/vehicles")
    public ResponseEntity<Vehicle> addVehicle(
            @PathVariable String driverId,
            @Valid @RequestBody CreateVehicleRequest request) {
        Vehicle vehicle = driverService.addVehicle(driverId, request);
        return new ResponseEntity<>(vehicle, HttpStatus.CREATED);
    }

    // 7. Get Vehicles of a Driver
    @GetMapping("/{driverId}/vehicles")
    public ResponseEntity<List<Vehicle>> getVehicles(@PathVariable String driverId) {
        return ResponseEntity.ok(driverService.getVehiclesByDriver(driverId));
    }
}