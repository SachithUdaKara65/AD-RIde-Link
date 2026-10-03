package com.ridelink.ridemanagement.service;

import com.ridelink.ridemanagement.dto.CreateRideRequest;
import com.ridelink.ridemanagement.dto.RideResponse;

import java.util.List;

public interface RideService {

    RideResponse requestRide(CreateRideRequest request);

    RideResponse assignDriver(String rideId, String driverId);

    RideResponse acceptRide(String rideId);

    RideResponse startRide(String rideId);

    RideResponse completeRide(String rideId);

    RideResponse cancelRide(String rideId);

    RideResponse getRideById(String rideId);

    List<RideResponse> getRidesByPassengerId(String passengerId);

    List<RideResponse> getRidesByDriverId(String driverId);
}
