package com.ridelink.ridemanagement.controller;

import com.ridelink.ridemanagement.dto.CreateRideRequest;
import com.ridelink.ridemanagement.dto.ErrorResponse;
import com.ridelink.ridemanagement.dto.RideResponse;
import com.ridelink.ridemanagement.service.RideService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Validated
@RestController
@RequestMapping("/api/rides")
@RequiredArgsConstructor
@Tag(name = "Ride Management", description = "Endpoints for managing ride lifecycle, state transitions, driver assignments, and histories")
public class RideController {

    private final RideService rideService;

    @Operation(summary = "Request a new ride", description = "Creates a new ride request with status REQUESTED")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Ride requested successfully",
                    content = @Content(schema = @Schema(implementation = RideResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation failed for request payload",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/request")
    public ResponseEntity<RideResponse> requestRide(@Valid @RequestBody CreateRideRequest request) {
        RideResponse response = rideService.requestRide(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @Operation(summary = "Assign driver to ride", description = "Assigns an available driver to a REQUESTED ride, transitioning to ASSIGNED")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Driver successfully assigned",
                    content = @Content(schema = @Schema(implementation = RideResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid transition or invalid driver ID",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Ride not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/{id}/assign")
    public ResponseEntity<RideResponse> assignDriver(
            @Parameter(description = "ID of the ride", example = "60d5ec49f1b2c82d88a1b2c3")
            @PathVariable("id") @NotBlank(message = "Ride ID must not be blank") String id,
            @Parameter(description = "ID of the driver to assign", example = "drv_98765")
            @RequestParam("driverId") @NotBlank(message = "Driver ID must not be blank") String driverId) {
        RideResponse response = rideService.assignDriver(id, driverId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Driver accepts ride", description = "Transitions ride from ASSIGNED to ACCEPTED")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Ride successfully accepted",
                    content = @Content(schema = @Schema(implementation = RideResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid transition state",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Ride not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/{id}/accept")
    public ResponseEntity<RideResponse> acceptRide(
            @Parameter(description = "ID of the ride", example = "60d5ec49f1b2c82d88a1b2c3")
            @PathVariable("id") @NotBlank(message = "Ride ID must not be blank") String id) {
        RideResponse response = rideService.acceptRide(id);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Start ride", description = "Transitions ride from ACCEPTED to IN_PROGRESS")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Ride successfully started",
                    content = @Content(schema = @Schema(implementation = RideResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid transition state",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Ride not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/{id}/start")
    public ResponseEntity<RideResponse> startRide(
            @Parameter(description = "ID of the ride", example = "60d5ec49f1b2c82d88a1b2c3")
            @PathVariable("id") @NotBlank(message = "Ride ID must not be blank") String id) {
        RideResponse response = rideService.startRide(id);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Complete ride", description = "Transitions ride from IN_PROGRESS to COMPLETED")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Ride successfully completed",
                    content = @Content(schema = @Schema(implementation = RideResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid transition state",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Ride not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/{id}/complete")
    public ResponseEntity<RideResponse> completeRide(
            @Parameter(description = "ID of the ride", example = "60d5ec49f1b2c82d88a1b2c3")
            @PathVariable("id") @NotBlank(message = "Ride ID must not be blank") String id) {
        RideResponse response = rideService.completeRide(id);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Cancel ride", description = "Cancels a ride. Only allowed if status is REQUESTED or ASSIGNED")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Ride successfully cancelled",
                    content = @Content(schema = @Schema(implementation = RideResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid transition state (not cancellable)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Ride not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/{id}/cancel")
    public ResponseEntity<RideResponse> cancelRide(
            @Parameter(description = "ID of the ride", example = "60d5ec49f1b2c82d88a1b2c3")
            @PathVariable("id") @NotBlank(message = "Ride ID must not be blank") String id) {
        RideResponse response = rideService.cancelRide(id);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get ride by ID", description = "Fetches details of a specific ride by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Ride details retrieved",
                    content = @Content(schema = @Schema(implementation = RideResponse.class))),
            @ApiResponse(responseCode = "404", description = "Ride not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<RideResponse> getRideById(
            @Parameter(description = "ID of the ride", example = "60d5ec49f1b2c82d88a1b2c3")
            @PathVariable("id") @NotBlank(message = "Ride ID must not be blank") String id) {
        RideResponse response = rideService.getRideById(id);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get passenger ride history", description = "Retrieves all rides requested by a specific passenger")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of passenger rides",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = RideResponse.class))))
    })
    @GetMapping("/passenger/{passengerId}")
    public ResponseEntity<List<RideResponse>> getRidesByPassenger(
            @Parameter(description = "ID of the passenger", example = "pass_12345")
            @PathVariable("passengerId") @NotBlank(message = "Passenger ID must not be blank") String passengerId) {
        List<RideResponse> responses = rideService.getRidesByPassengerId(passengerId);
        return ResponseEntity.ok(responses);
    }

    @Operation(summary = "Get driver assigned rides", description = "Retrieves all rides assigned to a specific driver")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of driver rides",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = RideResponse.class))))
    })
    @GetMapping("/driver/{driverId}")
    public ResponseEntity<List<RideResponse>> getRidesByDriver(
            @Parameter(description = "ID of the driver", example = "drv_98765")
            @PathVariable("driverId") @NotBlank(message = "Driver ID must not be blank") String driverId) {
        List<RideResponse> responses = rideService.getRidesByDriverId(driverId);
        return ResponseEntity.ok(responses);
    }
}
