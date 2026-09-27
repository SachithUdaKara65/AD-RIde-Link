package com.ridelink.drivervehicle.repository;

import com.ridelink.drivervehicle.model.Driver;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DriverRepository extends MongoRepository<Driver, String> {

    List<Driver> findByAvailabilityAndStatus(String availability, String status);

    List<Driver> findByAvailabilityAndStatusAndServiceArea(
            String availability,
            String status,
            String serviceArea);

    List<Driver> findByAccountId(String accountId);
}