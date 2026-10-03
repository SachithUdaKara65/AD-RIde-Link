package com.ridelink.drivervehicle.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "driver-service.service-token=test-service-token")
@AutoConfigureMockMvc
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser(roles = "ADMIN")
    void publicCreateDriverEndpoint_shouldNotBeAccessible() throws Exception {
        mockMvc.perform(post("/api/drivers"))
                .andExpect(status().isNotFound());
    }

    @Test
    void internalCreateDriverEndpoint_requiresServiceToken() throws Exception {
        mockMvc.perform(post("/api/drivers/internal")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "accountId": "acc-test",
                          "licenseNumber": "B1234567"
                        }
                        """))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void internalCreateDriverEndpoint_isNotListedInOpenApi() throws Exception {
        mockMvc.perform(get("/api-docs"))
                .andExpect(status().isOk())
                .andExpect(content().string(not(containsString("/api/drivers/internal"))));
    }
}
