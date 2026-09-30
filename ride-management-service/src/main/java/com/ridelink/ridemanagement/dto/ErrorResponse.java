package com.ridelink.ridemanagement.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Standard structured error response")
public class ErrorResponse {

    @Schema(description = "Timestamp when the error occurred", example = "2026-09-29T12:00:00")
    private LocalDateTime timestamp;

    @Schema(description = "HTTP status code", example = "400")
    private int status;

    @Schema(description = "HTTP reason phrase or category", example = "Bad Request")
    private String error;

    @Schema(description = "Detailed human-readable error message", example = "Cannot assign driver in status COMPLETED")
    private String message;

    @Schema(description = "Map of field validation errors, if applicable")
    private Map<String, String> validationErrors;
}
