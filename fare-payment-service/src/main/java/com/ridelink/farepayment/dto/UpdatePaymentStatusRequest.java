package com.ridelink.farepayment.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdatePaymentStatusRequest {

    @NotBlank(message = "Status is required")
    private String status;
}
