package com.ridelink.farepayment.repository;

import com.ridelink.farepayment.model.Fare;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FareRepository extends MongoRepository<Fare, String> {
    Optional<Fare> findByRideId(String rideId);
}
