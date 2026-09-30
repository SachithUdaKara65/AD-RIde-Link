package com.ridelink.ridemanagement.model;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Lifecycle status of a ride")
public enum RideStatus {
    @Schema(description = "Ride has been requested by the passenger")
    REQUESTED,

    @Schema(description = "A driver has been assigned to the ride")
    ASSIGNED,

    @Schema(description = "The assigned driver has accepted the ride")
    ACCEPTED,

    @Schema(description = "The ride is currently in progress")
    IN_PROGRESS,

    @Schema(description = "The ride has been completed successfully")
    COMPLETED,

    @Schema(description = "The ride has been cancelled")
    CANCELLED
}
