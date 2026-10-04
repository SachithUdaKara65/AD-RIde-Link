package com.ridelink.drivervehicle.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser(roles = "ADMIN")
    void createDriverEndpoint_shouldNotExist() throws Exception {
        mockMvc.perform(post("/api/drivers"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "PASSENGER")
    void provisionDriverProfile_shouldRequireDriverRole() throws Exception {
        mockMvc.perform(post("/api/drivers/me/profile")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"licenseNumber\":\"B1234567\",\"serviceArea\":\"Colombo\"}"))
                .andExpect(status().isForbidden());
    }
}
