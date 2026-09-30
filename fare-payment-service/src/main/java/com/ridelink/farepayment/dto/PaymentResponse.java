package com.ridelink.farepayment.dto;

import com.ridelink.farepayment.model.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponse {
    private String id;
    private String rideId;
    private String passengerId;
    private String driverId;
    private String fareId;
    private BigDecimal amount;
    private String currency;
    private PaymentStatus status;
    private String paymentMethod;
    private String transactionReference;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
