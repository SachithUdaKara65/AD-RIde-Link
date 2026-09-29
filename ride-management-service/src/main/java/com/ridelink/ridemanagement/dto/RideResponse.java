package com.ridelink.ridemanagement.dto;

import com.ridelink.ridemanagement.model.Ride;
import com.ridelink.ridemanagement.model.RideStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Details of a ride")
public class RideResponse {

    @Schema(description = "Unique identifier of the ride", example = "60d5ec49f1b2c82d88a1b2c3")
    private String id;

    @Schema(description = "Identifier of the passenger who requested the ride", example = "pass_12345")
    private String passengerId;

    @Schema(description = "Identifier of the assigned driver, null if not yet assigned", example = "drv_98765")
    private String driverId;

    @Schema(description = "Pickup location address or landmark", example = "Colombo Fort Railway Station")
    private String pickupLocation;

    @Schema(description = "Destination location address or landmark", example = "Bandaranaike International Airport")
    private String destinationLocation;

    @Schema(description = "Fare amount", example = "2500.00")
    private Double fare;

    @Schema(description = "Current lifecycle status of the ride", example = "REQUESTED")
    private RideStatus status;

    @Schema(description = "Timestamp when the ride was requested")
    private Date createdAt;

    @Schema(description = "Timestamp when the ride status was last updated")
    private Date updatedAt;

    public static RideResponse fromEntity(Ride ride) {
        if (ride == null) {
            return null;
        }
        return RideResponse.builder()
                .id(ride.getId())
                .passengerId(ride.getPassengerId())
                .driverId(ride.getDriverId())
                .pickupLocation(ride.getPickupLocation())
                .destinationLocation(ride.getDestinationLocation())
                .fare(ride.getFare())
                .status(ride.getStatus())
                .createdAt(ride.getCreatedAt())
                .updatedAt(ride.getUpdatedAt())
                .build();
    }
}
