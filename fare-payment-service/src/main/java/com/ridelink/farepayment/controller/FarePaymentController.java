package com.ridelink.farepayment.controller;

import com.ridelink.farepayment.dto.CreatePaymentRequest;
import com.ridelink.farepayment.dto.FareQuoteRequest;
import com.ridelink.farepayment.dto.FareQuoteResponse;
import com.ridelink.farepayment.dto.PaymentResponse;
import com.ridelink.farepayment.dto.UpdatePaymentStatusRequest;
import com.ridelink.farepayment.service.FarePaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Fare & Payment", description = "Fare calculation and payment operations")
public class FarePaymentController {

    private final FarePaymentService farePaymentService;

    @PostMapping("/fares/quote")
    @Operation(summary = "Generate a fare estimate for a ride")
    public ResponseEntity<FareQuoteResponse> createFareQuote(@Valid @RequestBody FareQuoteRequest request) {
        return ResponseEntity.ok(farePaymentService.createFareQuote(request));
    }

    @GetMapping("/fares/ride/{rideId}")
    @Operation(summary = "Get fare by ride ID")
    public ResponseEntity<FareQuoteResponse> getFareByRideId(@PathVariable String rideId) {
        return ResponseEntity.ok(farePaymentService.getFareByRideId(rideId));
    }

    @PostMapping("/payments")
    @Operation(summary = "Create a payment for a completed or pending ride")
    public ResponseEntity<PaymentResponse> createPayment(@Valid @RequestBody CreatePaymentRequest request) {
        return new ResponseEntity<>(farePaymentService.createPayment(request), HttpStatus.CREATED);
    }

    @GetMapping("/payments/{id}")
    @Operation(summary = "Get payment by ID")
    public ResponseEntity<PaymentResponse> getPaymentById(@PathVariable String id) {
        return ResponseEntity.ok(farePaymentService.getPaymentById(id));
    }

    @GetMapping("/payments/ride/{rideId}")
    @Operation(summary = "Get all payments for a ride")
    public ResponseEntity<List<PaymentResponse>> getPaymentsByRideId(@PathVariable String rideId) {
        return ResponseEntity.ok(farePaymentService.getPaymentsByRideId(rideId));
    }

    @PatchMapping("/payments/{id}/status")
    @Operation(summary = "Update payment status")
    public ResponseEntity<PaymentResponse> updatePaymentStatus(
            @PathVariable String id,
            @Valid @RequestBody UpdatePaymentStatusRequest request) {
        return ResponseEntity.ok(farePaymentService.updatePaymentStatus(id, request));
    }
}
