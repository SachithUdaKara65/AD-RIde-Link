package com.ridelink.account.services;

import com.ridelink.account.dto.UserResponse;
import com.ridelink.account.models.Role;
import com.ridelink.account.models.User;
import com.ridelink.account.repositories.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Test
    @DisplayName("Get User Profile - Success")
    void getUserProfile_Success() {
        User user = User.builder()
                .id("usr123")
                .name("Pathum Madhusanka")
                .email("pathum@example.com")
                .role(Role.PASSENGER)
                .status("ACTIVE")
                .build();

        when(userRepository.findById("usr123")).thenReturn(Optional.of(user));

        UserResponse response = userService.getUserProfile("usr123");

        assertNotNull(response);
        assertEquals("usr123", response.getId());
        assertEquals("Pathum Madhusanka", response.getName());
    }

    @Test
    @DisplayName("Get User Profile - User Not Found (Throws Exception)")
    void getUserProfile_NotFound_ThrowsException() {
        when(userRepository.findById("invalid_id")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () ->
                userService.getUserProfile("invalid_id")
        );
    }
}