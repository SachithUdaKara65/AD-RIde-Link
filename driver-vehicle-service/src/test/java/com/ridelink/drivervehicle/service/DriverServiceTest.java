package com.ridelink.drivervehicle.service;

import com.ridelink.drivervehicle.dto.CreateDriverRequest;
import com.ridelink.drivervehicle.dto.DriverResponse;
import com.ridelink.drivervehicle.dto.UpdateAvailabilityRequest;
import com.ridelink.drivervehicle.model.Vehicle;
import com.ridelink.drivervehicle.exception.ResourceNotFoundException;
import com.ridelink.drivervehicle.model.Driver;
import com.ridelink.drivervehicle.repository.DriverRepository;
import com.ridelink.drivervehicle.repository.VehicleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DriverServiceTest {

    @Mock
    private DriverRepository driverRepository;

    @Mock
    private VehicleRepository vehicleRepository;

    @InjectMocks
    private DriverService driverService;

    private Driver driver;

    @BeforeEach
    void setUp() {
        driver = new Driver();
        driver.setId("driver-001");
        driver.setAccountId("acc-001");
        driver.setLicenseNumber("B1234567");
        driver.setStatus("ACTIVE");
        driver.setAvailability("OFFLINE");
        driver.setServiceArea("Colombo");
        driver.setCreatedAt(LocalDateTime.now());
        driver.setUpdatedAt(LocalDateTime.now());
    }

    @Test
    void createDriver_ShouldReturnDriverResponse() {
        // Arrange
        CreateDriverRequest request = new CreateDriverRequest();
        request.setAccountId("acc-001");
        request.setLicenseNumber("B1234567");
        request.setServiceArea("Colombo");

        when(driverRepository.save(any(Driver.class))).thenReturn(driver);

        // Act
        DriverResponse response = driverService.createDriver(request);

        // Assert
        assertNotNull(response);
        assertEquals("acc-001", response.getAccountId());
        assertEquals("B1234567", response.getLicenseNumber());
        assertEquals("OFFLINE", response.getAvailability());
        verify(driverRepository).save(argThat(savedDriver ->
                "acc-001".equals(savedDriver.getAccountId())
                        && "B1234567".equals(savedDriver.getLicenseNumber())));
    }

    @Test
    void createDriver_WhenProfileAlreadyExists_ShouldReturnExistingProfile() {
        when(driverRepository.findByAccountId("acc-001")).thenReturn(List.of(driver));

        CreateDriverRequest request = new CreateDriverRequest();
        request.setAccountId("acc-001");
        request.setLicenseNumber("B1234567");

        DriverResponse response = driverService.createDriver(request);

        assertEquals(driver.getId(), response.getId());
        verify(driverRepository, never()).save(any(Driver.class));
    }

    @Test
    void getDriverById_WhenDriverExists_ShouldReturnDriver() {
        when(driverRepository.findById("driver-001")).thenReturn(Optional.of(driver));

        DriverResponse response = driverService.getDriverById("driver-001");

        assertNotNull(response);
        assertEquals("driver-001", response.getId());
        assertEquals("B1234567", response.getLicenseNumber());
    }

    @Test
    void getDriverById_WhenGivenAccountId_ShouldReturnLinkedDriverProfile() {
        when(driverRepository.findById("acc-001")).thenReturn(Optional.empty());
        when(driverRepository.findByAccountId("acc-001")).thenReturn(List.of(driver));

        DriverResponse response = driverService.getDriverById("acc-001");

        assertEquals("driver-001", response.getId());
        assertEquals("acc-001", response.getAccountId());
    }

    @Test
    void getDriverById_WhenDriverNotFound_ShouldThrowException() {
        when(driverRepository.findById("invalid-id")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            driverService.getDriverById("invalid-id");
        });
    }

    @Test
    void updateAvailability_ShouldUpdateSuccessfully() {
        when(driverRepository.findById("driver-001")).thenReturn(Optional.of(driver));
        when(driverRepository.save(any(Driver.class))).thenReturn(driver);

        UpdateAvailabilityRequest request = new UpdateAvailabilityRequest();
        request.setAvailability("AVAILABLE");

        DriverResponse response = driverService.updateAvailability("driver-001", request);

        assertEquals("AVAILABLE", response.getAvailability());
        verify(driverRepository, times(1)).save(any(Driver.class));
    }

    @Test
    void updateAvailability_WhenGivenAccountId_ShouldUpdateLinkedDriverProfile() {
        when(driverRepository.findById("acc-001")).thenReturn(Optional.empty());
        when(driverRepository.findByAccountId("acc-001")).thenReturn(List.of(driver));
        when(driverRepository.save(any(Driver.class))).thenReturn(driver);

        UpdateAvailabilityRequest request = new UpdateAvailabilityRequest();
        request.setAvailability("Available");

        DriverResponse response = driverService.updateAvailability("acc-001", request);

        assertEquals("AVAILABLE", response.getAvailability());
        verify(driverRepository).save(driver);
    }

    @Test
    void getVehiclesByDriver_WhenGivenAccountId_ShouldUseLinkedDriverProfileId() {
        Vehicle vehicle = new Vehicle();
        vehicle.setId("vehicle-001");
        vehicle.setDriverId("driver-001");
        when(driverRepository.findById("acc-001")).thenReturn(Optional.empty());
        when(driverRepository.findByAccountId("acc-001")).thenReturn(List.of(driver));
        when(vehicleRepository.findByDriverId("driver-001")).thenReturn(List.of(vehicle));

        List<Vehicle> result = driverService.getVehiclesByDriver("acc-001");

        assertEquals(List.of(vehicle), result);
        verify(vehicleRepository).findByDriverId("driver-001");
    }

    @Test
    void getAvailableDrivers_ShouldReturnList() {
        driver.setAvailability("AVAILABLE");
        when(driverRepository.findByAvailabilityAndStatus("AVAILABLE", "ACTIVE"))
                .thenReturn(List.of(driver));

        List<DriverResponse> result = driverService.getAvailableDrivers(null);

        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals("AVAILABLE", result.get(0).getAvailability());
    }

    @Test
    void getAvailableDrivers_ShouldTrimServiceAreaAndIgnoreCase() {
        driver.setAvailability("AVAILABLE");
        driver.setServiceArea("biyagama");
        when(driverRepository.findByAvailabilityAndStatusAndServiceAreaIgnoreCase(
                "AVAILABLE", "ACTIVE", "Biyagama"))
                .thenReturn(List.of(driver));

        List<DriverResponse> result = driverService.getAvailableDrivers(" Biyagama ");

        assertEquals(1, result.size());
        assertEquals("biyagama", result.get(0).getServiceArea());
        verify(driverRepository).findByAvailabilityAndStatusAndServiceAreaIgnoreCase(
                "AVAILABLE", "ACTIVE", "Biyagama");
    }
}