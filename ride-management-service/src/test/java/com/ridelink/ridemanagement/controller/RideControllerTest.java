package com.ridelink.ridemanagement.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ridelink.ridemanagement.dto.CreateRideRequest;
import com.ridelink.ridemanagement.dto.RideResponse;
import com.ridelink.ridemanagement.exception.GlobalExceptionHandler;
import com.ridelink.ridemanagement.exception.InvalidStateTransitionException;
import com.ridelink.ridemanagement.exception.ResourceNotFoundException;
import com.ridelink.ridemanagement.model.RideStatus;
import com.ridelink.ridemanagement.service.RideService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.util.Date;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class RideControllerTest {

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private RideService rideService;

    @InjectMocks
    private RideController rideController;

    private RideResponse sampleResponse;

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc = MockMvcBuilders.standaloneSetup(rideController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .build();

        sampleResponse = RideResponse.builder()
                .id("ride_123")
                .passengerId("pass_123")
                .driverId("drv_456")
                .pickupLocation("Central Bus Stand")
                .destinationLocation("Airport Road")
                .fare(1500.0)
                .status(RideStatus.REQUESTED)
                .createdAt(new Date())
                .updatedAt(new Date())
                .build();
    }

    @Test
    @DisplayName("POST /api/rides/request - 201 Created on valid request")
    void testRequestRide_Success() throws Exception {
        CreateRideRequest request = CreateRideRequest.builder()
                .passengerId("pass_123")
                .pickupLocation("Central Bus Stand")
                .destinationLocation("Airport Road")
                .fare(1500.0)
                .build();

        when(rideService.requestRide(any(CreateRideRequest.class))).thenReturn(sampleResponse);

        mockMvc.perform(post("/api/rides/request")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is("ride_123")))
                .andExpect(jsonPath("$.passengerId", is("pass_123")))
                .andExpect(jsonPath("$.status", is("REQUESTED")));
    }

    @Test
    @DisplayName("POST /api/rides/request - 400 Bad Request on invalid request body")
    void testRequestRide_ValidationFailure() throws Exception {
        CreateRideRequest invalidRequest = CreateRideRequest.builder()
                .passengerId("")
                .pickupLocation("")
                .destinationLocation("")
                .fare(-10.0)
                .build();

        mockMvc.perform(post("/api/rides/request")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.error", is("Validation Failed")))
                .andExpect(jsonPath("$.validationErrors.passengerId").exists())
                .andExpect(jsonPath("$.validationErrors.pickupLocation").exists())
                .andExpect(jsonPath("$.validationErrors.destinationLocation").exists())
                .andExpect(jsonPath("$.validationErrors.fare").exists());
    }

    @Test
    @DisplayName("POST /api/rides/{id}/assign - 200 OK on successful driver assignment")
    void testAssignDriver_Success() throws Exception {
        sampleResponse.setStatus(RideStatus.ASSIGNED);
        when(rideService.assignDriver("ride_123", "drv_456")).thenReturn(sampleResponse);

        mockMvc.perform(post("/api/rides/ride_123/assign")
                        .param("driverId", "drv_456"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is("ride_123")))
                .andExpect(jsonPath("$.status", is("ASSIGNED")))
                .andExpect(jsonPath("$.driverId", is("drv_456")));
    }

    @Test
    @DisplayName("POST /api/rides/{id}/assign - 400 Bad Request on invalid state transition")
    void testAssignDriver_InvalidStateTransition() throws Exception {
        when(rideService.assignDriver(eq("ride_123"), eq("drv_456")))
                .thenThrow(new InvalidStateTransitionException("Cannot assign driver. Ride is in status: ACCEPTED. Expected status: REQUESTED."));

        mockMvc.perform(post("/api/rides/ride_123/assign")
                        .param("driverId", "drv_456"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.error", is("Invalid State Transition")))
                .andExpect(jsonPath("$.message", containsString("Expected status: REQUESTED")));
    }

    @Test
    @DisplayName("POST /api/rides/{id}/assign - 404 Not Found when ride does not exist")
    void testAssignDriver_NotFound() throws Exception {
        when(rideService.assignDriver(eq("ride_not_found"), eq("drv_456")))
                .thenThrow(new ResourceNotFoundException("Ride not found with id: ride_not_found"));

        mockMvc.perform(post("/api/rides/ride_not_found/assign")
                        .param("driverId", "drv_456"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.error", is("Not Found")))
                .andExpect(jsonPath("$.message", containsString("Ride not found with id: ride_not_found")));
    }

    @Test
    @DisplayName("POST /api/rides/{id}/accept - 200 OK on acceptance")
    void testAcceptRide_Success() throws Exception {
        sampleResponse.setStatus(RideStatus.ACCEPTED);
        when(rideService.acceptRide("ride_123")).thenReturn(sampleResponse);

        mockMvc.perform(post("/api/rides/ride_123/accept"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is("ride_123")))
                .andExpect(jsonPath("$.status", is("ACCEPTED")));
    }

    @Test
    @DisplayName("POST /api/rides/{id}/start - 200 OK on start")
    void testStartRide_Success() throws Exception {
        sampleResponse.setStatus(RideStatus.IN_PROGRESS);
        when(rideService.startRide("ride_123")).thenReturn(sampleResponse);

        mockMvc.perform(post("/api/rides/ride_123/start"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is("ride_123")))
                .andExpect(jsonPath("$.status", is("IN_PROGRESS")));
    }

    @Test
    @DisplayName("POST /api/rides/{id}/complete - 200 OK on completion")
    void testCompleteRide_Success() throws Exception {
        sampleResponse.setStatus(RideStatus.COMPLETED);
        when(rideService.completeRide("ride_123")).thenReturn(sampleResponse);

        mockMvc.perform(post("/api/rides/ride_123/complete"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is("ride_123")))
                .andExpect(jsonPath("$.status", is("COMPLETED")));
    }

    @Test
    @DisplayName("POST /api/rides/{id}/cancel - 200 OK on cancellation")
    void testCancelRide_Success() throws Exception {
        sampleResponse.setStatus(RideStatus.CANCELLED);
        when(rideService.cancelRide("ride_123")).thenReturn(sampleResponse);

        mockMvc.perform(post("/api/rides/ride_123/cancel"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is("ride_123")))
                .andExpect(jsonPath("$.status", is("CANCELLED")));
    }

    @Test
    @DisplayName("POST /api/rides/{id}/cancel - 400 Bad Request when cancellation disallowed")
    void testCancelRide_Disallowed() throws Exception {
        when(rideService.cancelRide("ride_123"))
                .thenThrow(new InvalidStateTransitionException("Cannot cancel ride. Cancellation is only allowed when ride is in REQUESTED or ASSIGNED status. Current status: IN_PROGRESS."));

        mockMvc.perform(post("/api/rides/ride_123/cancel"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.error", is("Invalid State Transition")))
                .andExpect(jsonPath("$.message", containsString("Current status: IN_PROGRESS")));
    }

    @Test
    @DisplayName("GET /api/rides/{id} - 200 OK on existing ride")
    void testGetRideById_Success() throws Exception {
        when(rideService.getRideById("ride_123")).thenReturn(sampleResponse);

        mockMvc.perform(get("/api/rides/ride_123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is("ride_123")))
                .andExpect(jsonPath("$.passengerId", is("pass_123")));
    }

    @Test
    @DisplayName("GET /api/rides/{id} - 404 Not Found when ride does not exist")
    void testGetRideById_NotFound() throws Exception {
        when(rideService.getRideById("non_existing"))
                .thenThrow(new ResourceNotFoundException("Ride not found with id: non_existing"));

        mockMvc.perform(get("/api/rides/non_existing"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.error", is("Not Found")));
    }

    @Test
    @DisplayName("GET /api/rides/passenger/{passengerId} - 200 OK returns list")
    void testGetRidesByPassenger_Success() throws Exception {
        when(rideService.getRidesByPassengerId("pass_123")).thenReturn(List.of(sampleResponse));

        mockMvc.perform(get("/api/rides/passenger/pass_123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].passengerId", is("pass_123")));
    }

    @Test
    @DisplayName("GET /api/rides/driver/{driverId} - 200 OK returns list")
    void testGetRidesByDriver_Success() throws Exception {
        when(rideService.getRidesByDriverId("drv_456")).thenReturn(List.of(sampleResponse));

        mockMvc.perform(get("/api/rides/driver/drv_456"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].driverId", is("drv_456")));
    }
}
