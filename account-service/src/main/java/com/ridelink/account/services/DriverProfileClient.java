package com.ridelink.account.services;

import com.ridelink.account.dto.DriverProfileRequest;
import com.ridelink.account.exceptions.DriverProfileProvisioningException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class DriverProfileClient {

    private final RestClient restClient;
    private final String serviceToken;

    public DriverProfileClient(
            @Value("${driver-service.base-url:http://localhost:8082}") String baseUrl,
            @Value("${driver-service.service-token:}") String serviceToken) {
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
        this.serviceToken = serviceToken;
    }

    public void createProfile(DriverProfileRequest request) {
        if (serviceToken.isBlank()) {
            throw new DriverProfileProvisioningException(
                    "Driver service authentication is not configured; set DRIVER_SERVICE_TOKEN.");
        }

        try {
            restClient.post()
                    .uri("/api/drivers/internal")
                    .header("X-Service-Token", serviceToken)
                    .body(request)
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientException exception) {
            throw new DriverProfileProvisioningException(
                    "Could not create the driver profile in Driver & Vehicle Service.", exception);
        }
    }
}
