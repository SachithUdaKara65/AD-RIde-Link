package com.ridelink.drivervehicle.repository;


import com.ridelink.drivervehicle.model.Vehicle;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VehicleRepository extends MongoRepository<Vehicle, String> {

    List<Vehicle> findByDriverId(String driverId);

    List<Vehicle> findByDriverIdAndIsActiveTrue(String driverId);
}
