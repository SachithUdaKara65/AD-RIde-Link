package com.ridelink.ridemanagement.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI rideManagementOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("RideLink - Ride Management Service API")
                        .description("Microservice for managing ride lifecycle, driver assignments, state transitions, and ride histories.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("RideLink Team")
                                .email("support@ridelink.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://springdoc.org")));
    }
}
