package com.ridelink.farepayment.service;

import com.ridelink.farepayment.dto.CreatePaymentRequest;
import com.ridelink.farepayment.dto.FareQuoteRequest;
import com.ridelink.farepayment.dto.FareQuoteResponse;
import com.ridelink.farepayment.dto.PaymentResponse;
import com.ridelink.farepayment.model.Fare;
import com.ridelink.farepayment.model.Payment;
import com.ridelink.farepayment.model.PaymentStatus;
import com.ridelink.farepayment.repository.FareRepository;
import com.ridelink.farepayment.repository.PaymentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FarePaymentServiceTest {

    @Mock
    private FareRepository fareRepository;

    @Mock
    private PaymentRepository paymentRepository;

    @InjectMocks
    private FarePaymentService farePaymentService;

    @Test
    void shouldCreateFareQuoteWithCalculatedAmounts() {
        FareQuoteRequest request = new FareQuoteRequest();
        request.setRideId("ride-001");
        request.setPassengerId("passenger-001");
        request.setDriverId("driver-001");
        request.setDistanceKm(12.5);
        request.setDurationMinutes(20);
        request.setServiceType("STANDARD");

        Fare savedFare = Fare.builder()
                .id("fare-001")
                .rideId(request.getRideId())
                .passengerId(request.getPassengerId())
                .driverId(request.getDriverId())
                .serviceType("STANDARD")
                .distanceKm(12.5)
                .durationMinutes(20)
                .baseFare(new BigDecimal("150.00"))
                .distanceFare(new BigDecimal("300.00"))
                .timeFare(new BigDecimal("170.00"))
                .surgeMultiplier(BigDecimal.ONE)
                .totalFare(new BigDecimal("620.00"))
                .currency("LKR")
                .build();

        when(fareRepository.save(any(Fare.class))).thenReturn(savedFare);

        FareQuoteResponse response = farePaymentService.createFareQuote(request);

        assertNotNull(response);
        assertEquals("fare-001", response.getQuoteId());
        assertEquals("620.00", response.getTotalFare().toPlainString());
        assertEquals("LKR", response.getCurrency());
    }

    @Test
    void shouldCreatePaymentWithPendingStatus() {
        CreatePaymentRequest request = new CreatePaymentRequest();
        request.setRideId("ride-002");
        request.setPassengerId("passenger-002");
        request.setDriverId("driver-002");
        request.setAmount(new BigDecimal("620.00"));
        request.setPaymentMethod("CARD");

        Payment savedPayment = Payment.builder()
                .id("payment-001")
                .rideId(request.getRideId())
                .passengerId(request.getPassengerId())
                .driverId(request.getDriverId())
                .amount(request.getAmount())
                .currency("LKR")
                .status(PaymentStatus.PENDING)
                .paymentMethod("CARD")
                .transactionReference("txn-123")
                .build();

        when(paymentRepository.save(any(Payment.class))).thenReturn(savedPayment);

        PaymentResponse response = farePaymentService.createPayment(request);

        assertNotNull(response);
        assertEquals("payment-001", response.getId());
        assertEquals(PaymentStatus.PENDING, response.getStatus());
        assertEquals("620.00", response.getAmount().toPlainString());
    }
}
