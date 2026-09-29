package com.ridelink.ridemanagement.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request payload to create a new ride request")
public class CreateRideRequest {

    @NotBlank(message = "Passenger ID is required")
    @Schema(description = "Unique ID of the passenger requesting the ride", example = "pass_12345")
    private String passengerId;

    @NotBlank(message = "Pickup location is required")
    @Schema(description = "Pickup location address or landmark", example = "Colombo Fort Railway Station")
    private String pickupLocation;

    @NotBlank(message = "Destination location is required")
    @Schema(description = "Destination location address or landmark", example = "Bandaranaike International Airport")
    private String destinationLocation;

    @NotNull(message = "Fare is required")
    @Positive(message = "Fare must be greater than zero")
    @Schema(description = "Estimated or agreed fare amount in LKR", example = "2500.00")
    private Double fare;
}
