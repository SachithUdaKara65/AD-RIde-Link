package com.ridelink.drivervehicle.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ridelink.drivervehicle.dto.CreateDriverRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createDriverEndpoint_shouldBeAccessibleWithoutAuthenticationForSwaggerTesting() throws Exception {
        CreateDriverRequest request = new CreateDriverRequest();
        request.setAccountId("acc-100");
        request.setFullName("Test Driver");
        request.setPhone("0771234567");
        request.setLicenseNumber("B1234567");
        request.setServiceArea("Colombo");

        mockMvc.perform(post("/api/drivers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }
}
