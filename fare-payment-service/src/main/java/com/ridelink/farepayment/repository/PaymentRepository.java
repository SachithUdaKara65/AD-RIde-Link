package com.ridelink.farepayment.repository;

import com.ridelink.farepayment.model.Payment;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends MongoRepository<Payment, String> {
    List<Payment> findByRideId(String rideId);
    Optional<Payment> findByRideIdAndPassengerId(String rideId, String passengerId);
}
