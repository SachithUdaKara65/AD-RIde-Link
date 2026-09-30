package com.ridelink.farepayment.service;

import com.ridelink.farepayment.dto.CreatePaymentRequest;
import com.ridelink.farepayment.dto.FareQuoteRequest;
import com.ridelink.farepayment.dto.FareQuoteResponse;
import com.ridelink.farepayment.dto.PaymentResponse;
import com.ridelink.farepayment.dto.UpdatePaymentStatusRequest;
import com.ridelink.farepayment.exception.ResourceNotFoundException;
import com.ridelink.farepayment.model.Fare;
import com.ridelink.farepayment.model.Payment;
import com.ridelink.farepayment.model.PaymentStatus;
import com.ridelink.farepayment.repository.FareRepository;
import com.ridelink.farepayment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FarePaymentService {

    private final FareRepository fareRepository;
    private final PaymentRepository paymentRepository;

    public FareQuoteResponse createFareQuote(FareQuoteRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Fare quote request is required");
        }
        validateQuoteRequest(request);

        String serviceType = request.getServiceType().trim().toUpperCase();
        BigDecimal baseFare = new BigDecimal("150.00");
        BigDecimal distanceRate = getDistanceRate(serviceType);
        BigDecimal timeRate = new BigDecimal("8.50");
        BigDecimal surgeMultiplier = getSurgeMultiplier(serviceType);

        BigDecimal distanceFare = BigDecimal.valueOf(request.getDistanceKm())
                .multiply(distanceRate)
                .setScale(2, RoundingMode.HALF_UP);

        BigDecimal timeFare = BigDecimal.valueOf(request.getDurationMinutes())
                .multiply(timeRate)
                .setScale(2, RoundingMode.HALF_UP);

        BigDecimal totalFare = baseFare
                .add(distanceFare)
                .add(timeFare)
                .multiply(surgeMultiplier)
                .setScale(2, RoundingMode.HALF_UP);

        Fare fare = Fare.builder()
                .rideId(request.getRideId())
                .passengerId(request.getPassengerId())
                .driverId(request.getDriverId())
                .serviceType(serviceType)
                .distanceKm(request.getDistanceKm())
                .durationMinutes(request.getDurationMinutes())
                .baseFare(baseFare)
                .distanceFare(distanceFare)
                .timeFare(timeFare)
                .surgeMultiplier(surgeMultiplier)
                .totalFare(totalFare)
                .currency("LKR")
                .build();

        Fare saved = fareRepository.save(fare);
        return mapToQuoteResponse(saved);
    }

    public FareQuoteResponse getFareByRideId(String rideId) {
        requireText(rideId, "Ride ID");
        Fare fare = fareRepository.findByRideId(rideId)
                .orElseThrow(() -> new ResourceNotFoundException("Fare not found for ride ID: " + rideId));
        return mapToQuoteResponse(fare);
    }

    public PaymentResponse createPayment(CreatePaymentRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Payment request is required");
        }
        requireText(request.getRideId(), "Ride ID");
        requireText(request.getPassengerId(), "Passenger ID");
        requireText(request.getDriverId(), "Driver ID");
        requireText(request.getPaymentMethod(), "Payment method");
        if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Payment amount must be greater than zero");
        }

        Payment payment = Payment.builder()
                .rideId(request.getRideId())
                .passengerId(request.getPassengerId())
                .driverId(request.getDriverId())
                .fareId(request.getFareId())
                .amount(request.getAmount())
                .currency("LKR")
                .status(PaymentStatus.PENDING)
                .paymentMethod(request.getPaymentMethod().trim().toUpperCase())
                .transactionReference(UUID.randomUUID().toString())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Payment saved = paymentRepository.save(payment);
        return mapToPaymentResponse(saved);
    }

    public PaymentResponse getPaymentById(String id) {
        requireText(id, "Payment ID");
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with id: " + id));
        return mapToPaymentResponse(payment);
    }

    public List<PaymentResponse> getPaymentsByRideId(String rideId) {
        requireText(rideId, "Ride ID");
        return paymentRepository.findByRideId(rideId).stream()
                .map(this::mapToPaymentResponse)
                .toList();
    }

    public PaymentResponse updatePaymentStatus(String id, UpdatePaymentStatusRequest request) {
        requireText(id, "Payment ID");
        if (request == null) {
            throw new IllegalArgumentException("Payment status request is required");
        }
        requireText(request.getStatus(), "Payment status");
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with id: " + id));

        PaymentStatus status = PaymentStatus.valueOf(request.getStatus().trim().toUpperCase());
        payment.setStatus(status);
        payment.setUpdatedAt(LocalDateTime.now());

        Payment updated = paymentRepository.save(payment);
        return mapToPaymentResponse(updated);
    }

    private void validateQuoteRequest(FareQuoteRequest request) {
        requireText(request.getRideId(), "Ride ID");
        requireText(request.getPassengerId(), "Passenger ID");
        requireText(request.getDriverId(), "Driver ID");
        requireText(request.getServiceType(), "Service type");
        if (request.getDistanceKm() == null || request.getDistanceKm() <= 0) {
            throw new IllegalArgumentException("Distance must be greater than zero");
        }

        if (request.getDurationMinutes() == null || request.getDurationMinutes() <= 0) {
            throw new IllegalArgumentException("Duration must be greater than zero");
        }

        String serviceType = request.getServiceType().trim().toUpperCase();
        if (!serviceType.equals("STANDARD") && !serviceType.equals("PREMIUM") && !serviceType.equals("XL")) {
            throw new IllegalArgumentException("Unsupported service type: " + request.getServiceType());
        }
    }

    private void requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " is required");
        }
    }

    private BigDecimal getDistanceRate(String serviceType) {
        return switch (serviceType) {
            case "STANDARD" -> new BigDecimal("24.00");
            case "PREMIUM" -> new BigDecimal("36.00");
            case "XL" -> new BigDecimal("42.00");
            default -> new BigDecimal("24.00");
        };
    }

    private BigDecimal getSurgeMultiplier(String serviceType) {
        return switch (serviceType) {
            case "PREMIUM" -> new BigDecimal("1.25");
            case "XL" -> new BigDecimal("1.40");
            default -> BigDecimal.ONE;
        };
    }

    private FareQuoteResponse mapToQuoteResponse(Fare fare) {
        return FareQuoteResponse.builder()
                .quoteId(fare.getId())
                .rideId(fare.getRideId())
                .passengerId(fare.getPassengerId())
                .driverId(fare.getDriverId())
                .distanceKm(fare.getDistanceKm())
                .durationMinutes(fare.getDurationMinutes())
                .serviceType(fare.getServiceType())
                .baseFare(fare.getBaseFare())
                .distanceFare(fare.getDistanceFare())
                .timeFare(fare.getTimeFare())
                .surgeMultiplier(fare.getSurgeMultiplier())
                .totalFare(fare.getTotalFare())
                .currency(fare.getCurrency())
                .createdAt(fare.getCreatedAt())
                .build();
    }

    private PaymentResponse mapToPaymentResponse(Payment payment) {
        return PaymentResponse.builder()
                .id(payment.getId())
                .rideId(payment.getRideId())
                .passengerId(payment.getPassengerId())
                .driverId(payment.getDriverId())
                .fareId(payment.getFareId())
                .amount(payment.getAmount())
                .currency(payment.getCurrency())
                .status(payment.getStatus())
                .paymentMethod(payment.getPaymentMethod())
                .transactionReference(payment.getTransactionReference())
                .createdAt(payment.getCreatedAt())
                .updatedAt(payment.getUpdatedAt())
                .build();
    }
}
