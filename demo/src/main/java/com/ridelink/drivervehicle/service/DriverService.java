package com.ridelink.drivervehicle.service;

import com.ridelink.drivervehicle.dto.*;
import com.ridelink.drivervehicle.model.Driver;
import com.ridelink.drivervehicle.model.Vehicle;
import com.ridelink.drivervehicle.repository.DriverRepository;
import com.ridelink.drivervehicle.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DriverService {

    private final DriverRepository driverRepository;
    private final VehicleRepository vehicleRepository;

    // Create Driver Profile
    public DriverResponse createDriver(CreateDriverRequest request) {
        Driver driver = new Driver();
        driver.setAccountId(request.getAccountId());
        driver.setFullName(request.getFullName());
        driver.setPhone(request.getPhone());
        driver.setLicenseNumber(request.getLicenseNumber());
        driver.setServiceArea(request.getServiceArea());
        driver.setStatus("ACTIVE");
        driver.setAvailability("OFFLINE");
        driver.setCreatedAt(LocalDateTime.now());
        driver.setUpdatedAt(LocalDateTime.now());

        Driver saved = driverRepository.save(driver);
        return mapToResponse(saved);
    }

    // Get Driver by ID
    public DriverResponse getDriverById(String id) {
        Driver driver = driverRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Driver not found with id: " + id));
        return mapToResponse(driver);
    }

    // Update Availability
    public DriverResponse updateAvailability(String id, UpdateAvailabilityRequest request) {
        Driver driver = driverRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Driver not found with id: " + id));

        String availability = request.getAvailability().toUpperCase();
        if (!List.of("AVAILABLE", "BUSY", "OFFLINE").contains(availability)) {
            throw new RuntimeException("Invalid availability. Allowed values: AVAILABLE, BUSY, OFFLINE");
        }

        driver.setAvailability(availability);
        driver.setUpdatedAt(LocalDateTime.now());

        Driver updated = driverRepository.save(driver);
        return mapToResponse(updated);
    }

    // Update Location
    public DriverResponse updateLocation(String id, UpdateLocationRequest request) {
        Driver driver = driverRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Driver not found with id: " + id));

        driver.setCurrentLatitude(request.getLatitude());
        driver.setCurrentLongitude(request.getLongitude());
        driver.setUpdatedAt(LocalDateTime.now());

        Driver updated = driverRepository.save(driver);
        return mapToResponse(updated);
    }

    // Get Available Drivers (Important for Ride Management Service)
    public List<DriverResponse> getAvailableDrivers(String serviceArea) {
        List<Driver> drivers;

        if (serviceArea != null && !serviceArea.isBlank()) {
            drivers = driverRepository.findByAvailabilityAndStatusAndServiceArea(
                    "AVAILABLE", "ACTIVE", serviceArea);
        } else {
            drivers = driverRepository.findByAvailabilityAndStatus("AVAILABLE", "ACTIVE");
        }

        return drivers.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // Add Vehicle to Driver
    public Vehicle addVehicle(String driverId, CreateVehicleRequest request) {
        // Check if driver exists
        driverRepository.findById(driverId)
                .orElseThrow(() -> new RuntimeException("Driver not found with id: " + driverId));

        Vehicle vehicle = new Vehicle();
        vehicle.setDriverId(driverId);
        vehicle.setMake(request.getMake());
        vehicle.setModel(request.getModel());
        vehicle.setYear(request.getYear());
        vehicle.setColor(request.getColor());
        vehicle.setLicensePlate(request.getLicensePlate());
        vehicle.setVehicleType(request.getVehicleType() != null ? request.getVehicleType() : "CAR");
        vehicle.setActive(true);

        return vehicleRepository.save(vehicle);
    }

    // Get Vehicles of a Driver
    public List<Vehicle> getVehiclesByDriver(String driverId) {
        return vehicleRepository.findByDriverId(driverId);
    }

    // Helper method to convert Entity → Response DTO
    private DriverResponse mapToResponse(Driver driver) {
        DriverResponse response = new DriverResponse();
        response.setId(driver.getId());
        response.setAccountId(driver.getAccountId());
        response.setFullName(driver.getFullName());
        response.setPhone(driver.getPhone());
        response.setLicenseNumber(driver.getLicenseNumber());
        response.setStatus(driver.getStatus());
        response.setAvailability(driver.getAvailability());
        response.setServiceArea(driver.getServiceArea());
        response.setCurrentLatitude(driver.getCurrentLatitude());
        response.setCurrentLongitude(driver.getCurrentLongitude());
        response.setCreatedAt(driver.getCreatedAt());
        response.setUpdatedAt(driver.getUpdatedAt());
        return response;
    }
}