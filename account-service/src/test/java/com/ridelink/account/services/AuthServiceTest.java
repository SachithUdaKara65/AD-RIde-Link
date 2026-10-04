package com.ridelink.account.services;

import com.ridelink.account.dto.AuthResponse;
import com.ridelink.account.dto.DriverProfileProvisionResponse;
import com.ridelink.account.dto.LoginRequest;
import com.ridelink.account.dto.RegisterRequest;
import com.ridelink.account.models.Role;
import com.ridelink.account.models.User;
import com.ridelink.account.repositories.UserRepository;
import com.ridelink.account.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private DriverProfileClient driverProfileClient;

    @InjectMocks
    private AuthService authService;

    private User sampleUser;
    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        sampleUser = User.builder()
                .id("usr123")
                .name("Pathum Madhusanka")
                .email("pathum@example.com")
                .password("encoded_pass")
                .role(Role.PASSENGER)
                .status("ACTIVE")
                .build();

        registerRequest = new RegisterRequest();
        registerRequest.setName("Pathum Madhusanka");
        registerRequest.setEmail("pathum@example.com");
        registerRequest.setPassword("raw_password");
        registerRequest.setRole(Role.PASSENGER);

        loginRequest = new LoginRequest();
        loginRequest.setEmail("pathum@example.com");
        loginRequest.setPassword("raw_password");
    }

    @Test
    @DisplayName("Register User - Success")
    void register_Success() {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encoded_pass");
        when(userRepository.save(any(User.class))).thenReturn(sampleUser);
        when(jwtTokenProvider.generateToken(anyString(), anyString(), anyString())).thenReturn("mock_token");

        AuthResponse response = authService.register(registerRequest);

        assertNotNull(response);
        assertEquals("mock_token", response.getToken());
        assertEquals("pathum@example.com", response.getEmail());
        verify(userRepository, times(1)).save(any(User.class));
        verifyNoInteractions(driverProfileClient);
    }

    @Test
    @DisplayName("Register Driver - Does Not Require Profile Details")
    void register_Driver_DoesNotRequireLicenseOrServiceArea() {
        registerRequest.setRole(Role.DRIVER);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encoded_pass");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId("usr-driver");
            return user;
        });
        when(jwtTokenProvider.generateToken("usr-driver", "pathum@example.com", "DRIVER"))
                .thenReturn("driver_token");
        AuthResponse response = authService.register(registerRequest);

        assertEquals("usr-driver", response.getUserId());
        assertNull(response.getDriverId());
        verifyNoInteractions(driverProfileClient);
    }

    @Test
    @DisplayName("Register User - Email Already Exists (Throws Exception)")
    void register_DuplicateEmail_ThrowsException() {
        when(userRepository.existsByEmail("pathum@example.com")).thenReturn(true);

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                authService.register(registerRequest)
        );

        assertTrue(exception.getMessage().contains("දැනටමත් ලියාපදිංචි කර ඇත"));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Login User - Success")
    void login_Success() {
        when(userRepository.findByEmail("pathum@example.com")).thenReturn(Optional.of(sampleUser));
        when(passwordEncoder.matches("raw_password", "encoded_pass")).thenReturn(true);
        when(jwtTokenProvider.generateToken(anyString(), anyString(), anyString())).thenReturn("mock_token");

        AuthResponse response = authService.login(loginRequest);

        assertNotNull(response);
        assertEquals("mock_token", response.getToken());
        assertEquals("usr123", response.getUserId());
        assertNull(response.getDriverId());
        verifyNoInteractions(driverProfileClient);
    }

    @Test
    @DisplayName("Login Driver - Returns Driver Profile ID")
    void login_Driver_ReturnsDriverId() {
        sampleUser.setRole(Role.DRIVER);
        when(userRepository.findByEmail("pathum@example.com")).thenReturn(Optional.of(sampleUser));
        when(passwordEncoder.matches("raw_password", "encoded_pass")).thenReturn(true);
        when(jwtTokenProvider.generateToken("usr123", "pathum@example.com", "DRIVER"))
                .thenReturn("driver_token");
        when(driverProfileClient.getProfile("driver_token"))
                .thenReturn(new DriverProfileProvisionResponse("driver-profile-123"));

        AuthResponse response = authService.login(loginRequest);

        assertEquals("driver-profile-123", response.getDriverId());
        verify(driverProfileClient).getProfile("driver_token");
    }

    @Test
    @DisplayName("Login Driver - Allows Login Before Profile Is Created")
    void login_DriverWithoutProfile_ReturnsTokenAndNullDriverId() {
        sampleUser.setRole(Role.DRIVER);
        when(userRepository.findByEmail("pathum@example.com")).thenReturn(Optional.of(sampleUser));
        when(passwordEncoder.matches("raw_password", "encoded_pass")).thenReturn(true);
        when(jwtTokenProvider.generateToken("usr123", "pathum@example.com", "DRIVER"))
                .thenReturn("driver_token");
        when(driverProfileClient.getProfile("driver_token"))
                .thenReturn(null);

        AuthResponse response = authService.login(loginRequest);

        assertEquals("driver_token", response.getToken());
        assertNull(response.getDriverId());
    }

    @Test
    @DisplayName("Login User - Invalid Password (Throws Exception)")
    void login_InvalidPassword_ThrowsException() {
        when(userRepository.findByEmail("pathum@example.com")).thenReturn(Optional.of(sampleUser));
        when(passwordEncoder.matches("raw_password", "encoded_pass")).thenReturn(false);

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                authService.login(loginRequest)
        );

        assertEquals("මුරපදය වැරදියි!", exception.getMessage());
    }
}