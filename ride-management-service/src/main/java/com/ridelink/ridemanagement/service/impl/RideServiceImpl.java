package com.ridelink.ridemanagement.service.impl;

import com.ridelink.ridemanagement.dto.CreateRideRequest;
import com.ridelink.ridemanagement.dto.RideResponse;
import com.ridelink.ridemanagement.exception.InvalidStateTransitionException;
import com.ridelink.ridemanagement.exception.ResourceNotFoundException;
import com.ridelink.ridemanagement.model.Ride;
import com.ridelink.ridemanagement.model.RideStatus;
import com.ridelink.ridemanagement.repository.RideRepository;
import com.ridelink.ridemanagement.service.RideService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RideServiceImpl implements RideService {

    private final RideRepository rideRepository;

    @Override
    public RideResponse requestRide(CreateRideRequest request) {
        validateRideRequest(request);

        log.info("Requesting ride for passenger ID: {}", request.getPassengerId().trim());
        Date now = new Date();
        Ride ride = Ride.builder()
                .passengerId(request.getPassengerId().trim())
                .pickupLocation(request.getPickupLocation().trim())
                .destinationLocation(request.getDestinationLocation().trim())
                .fare(request.getFare())
                .status(RideStatus.REQUESTED)
                .createdAt(now)
                .updatedAt(now)
                .build();

        Ride savedRide = rideRepository.save(ride);
        log.info("Ride requested successfully with ID: {}", savedRide.getId());
        return RideResponse.fromEntity(savedRide);
    }

    @Override
    public RideResponse assignDriver(String rideId, String driverId) {
        validateRequiredText(rideId, "Ride ID");
        validateRequiredText(driverId, "Driver ID");

        log.info("Assigning driver ID: {} to ride ID: {}", driverId.trim(), rideId.trim());

        Ride ride = findRideByIdOrThrow(rideId);

        if (ride.getStatus() != RideStatus.REQUESTED) {
            throw new InvalidStateTransitionException(
                    "Cannot assign driver to ride '" + rideId + "'. Ride is currently in status: "
                            + ride.getStatus() + ". Expected status: REQUESTED."
            );
        }

        ride.setDriverId(driverId.trim());
        ride.setStatus(RideStatus.ASSIGNED);
        ride.setUpdatedAt(new Date());

        Ride updatedRide = rideRepository.save(ride);
        log.info("Driver assigned successfully to ride ID: {}", updatedRide.getId());
        return RideResponse.fromEntity(updatedRide);
    }

    @Override
    public RideResponse acceptRide(String rideId) {
        validateRequiredText(rideId, "Ride ID");
        log.info("Driver accepting ride ID: {}", rideId.trim());
        Ride ride = findRideByIdOrThrow(rideId);

        if (ride.getStatus() != RideStatus.ASSIGNED) {
            throw new InvalidStateTransitionException(
                    "Cannot accept ride '" + rideId + "'. Ride is currently in status: "
                            + ride.getStatus() + ". Expected status: ASSIGNED."
            );
        }

        ride.setStatus(RideStatus.ACCEPTED);
        ride.setUpdatedAt(new Date());

        Ride updatedRide = rideRepository.save(ride);
        log.info("Ride ID: {} accepted successfully", updatedRide.getId());
        return RideResponse.fromEntity(updatedRide);
    }

    @Override
    public RideResponse startRide(String rideId) {
        validateRequiredText(rideId, "Ride ID");
        log.info("Starting ride ID: {}", rideId.trim());
        Ride ride = findRideByIdOrThrow(rideId);

        if (ride.getStatus() != RideStatus.ACCEPTED) {
            throw new InvalidStateTransitionException(
                    "Cannot start ride '" + rideId + "'. Ride is currently in status: "
                            + ride.getStatus() + ". Expected status: ACCEPTED."
            );
        }

        ride.setStatus(RideStatus.IN_PROGRESS);
        ride.setUpdatedAt(new Date());

        Ride updatedRide = rideRepository.save(ride);
        log.info("Ride ID: {} started successfully and is now IN_PROGRESS", updatedRide.getId());
        return RideResponse.fromEntity(updatedRide);
    }

    @Override
    public RideResponse completeRide(String rideId) {
        validateRequiredText(rideId, "Ride ID");
        log.info("Completing ride ID: {}", rideId.trim());
        Ride ride = findRideByIdOrThrow(rideId);

        if (ride.getStatus() != RideStatus.IN_PROGRESS) {
            throw new InvalidStateTransitionException(
                    "Cannot complete ride '" + rideId + "'. Ride is currently in status: "
                            + ride.getStatus() + ". Expected status: IN_PROGRESS."
            );
        }

        ride.setStatus(RideStatus.COMPLETED);
        ride.setUpdatedAt(new Date());

        Ride updatedRide = rideRepository.save(ride);
        log.info("Ride ID: {} completed successfully", updatedRide.getId());
        return RideResponse.fromEntity(updatedRide);
    }

    @Override
    public RideResponse cancelRide(String rideId) {
        validateRequiredText(rideId, "Ride ID");
        log.info("Attempting to cancel ride ID: {}", rideId.trim());
        Ride ride = findRideByIdOrThrow(rideId);

        if (ride.getStatus() != RideStatus.REQUESTED && ride.getStatus() != RideStatus.ASSIGNED) {
            throw new InvalidStateTransitionException(
                    "Cannot cancel ride '" + rideId + "'. Cancellation is only allowed when ride is in "
                            + "REQUESTED or ASSIGNED status. Current status: " + ride.getStatus() + "."
            );
        }

        ride.setStatus(RideStatus.CANCELLED);
        ride.setUpdatedAt(new Date());

        Ride updatedRide = rideRepository.save(ride);
        log.info("Ride ID: {} cancelled successfully", updatedRide.getId());
        return RideResponse.fromEntity(updatedRide);
    }

    @Override
    public RideResponse getRideById(String rideId) {
        validateRequiredText(rideId, "Ride ID");
        log.info("Fetching ride details for ID: {}", rideId.trim());
        Ride ride = findRideByIdOrThrow(rideId);
        return RideResponse.fromEntity(ride);
    }

    @Override
    public List<RideResponse> getRidesByPassengerId(String passengerId) {
        validateRequiredText(passengerId, "Passenger ID");
        log.info("Fetching ride history for passenger ID: {}", passengerId.trim());
        return rideRepository.findByPassengerId(passengerId.trim()).stream()
                .map(RideResponse::fromEntity)
                .toList();
    }

    @Override
    public List<RideResponse> getRidesByDriverId(String driverId) {
        validateRequiredText(driverId, "Driver ID");
        log.info("Fetching assigned rides for driver ID: {}", driverId.trim());
        return rideRepository.findByDriverId(driverId.trim()).stream()
                .map(RideResponse::fromEntity)
                .toList();
    }

    private void validateRideRequest(CreateRideRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Ride request must not be null");
        }

        validateRequiredText(request.getPassengerId(), "Passenger ID");
        validateRequiredText(request.getPickupLocation(), "Pickup location");
        validateRequiredText(request.getDestinationLocation(), "Destination location");

        if (request.getFare() == null) {
            throw new IllegalArgumentException("Fare is required");
        }

        if (request.getFare() <= 0) {
            throw new IllegalArgumentException("Fare must be greater than zero");
        }
    }

    private void validateRequiredText(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
    }

    private Ride findRideByIdOrThrow(String rideId) {
        validateRequiredText(rideId, "Ride ID");
        return rideRepository.findById(rideId.trim())
                .orElseThrow(() -> new ResourceNotFoundException("Ride not found with id: " + rideId.trim()));
    }
}
