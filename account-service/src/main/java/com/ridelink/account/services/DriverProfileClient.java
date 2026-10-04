package com.ridelink.account.services;

import com.ridelink.account.dto.DriverProfileProvisionResponse;
import com.ridelink.account.dto.DriverProfileProvisionRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

@Component
public class DriverProfileClient {

    private final RestClient restClient;

    public DriverProfileClient(
            @Value("${services.driver-vehicle.base-url:http://localhost:8082}") String baseUrl) {
        this.restClient = RestClient.create(baseUrl);
    }

    public DriverProfileProvisionResponse provisionProfile(
            String token,
            String licenseNumber,
            String serviceArea) {
        return restClient.post()
                .uri("/api/drivers/me/profile")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .body(new DriverProfileProvisionRequest(licenseNumber, serviceArea))
                .retrieve()
                .body(DriverProfileProvisionResponse.class);
    }

    public DriverProfileProvisionResponse getProfile(String token) {
        try {
            return restClient.get()
                    .uri("/api/drivers/me/profile")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                    .retrieve()
                    .body(DriverProfileProvisionResponse.class);
        } catch (HttpClientErrorException.NotFound ex) {
            return null;
        }
    }
}
