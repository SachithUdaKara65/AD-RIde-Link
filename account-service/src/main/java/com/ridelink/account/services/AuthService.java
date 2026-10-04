package com.ridelink.account.services;

import com.ridelink.account.dto.AuthResponse;
import com.ridelink.account.dto.DriverProfileProvisionResponse;
import com.ridelink.account.dto.LoginRequest;
import com.ridelink.account.dto.RegisterRequest;
import com.ridelink.account.exceptions.DriverProfileProvisioningException;
import com.ridelink.account.models.Role;
import com.ridelink.account.models.User;
import com.ridelink.account.repositories.UserRepository;
import com.ridelink.account.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final DriverProfileClient driverProfileClient;

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("This email address is already registered!");
        }

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .phone(request.getPhone())
                .role(request.getRole())
                .status("ACTIVE")
                .build();

        User savedUser = userRepository.save(user);
        String token = jwtTokenProvider.generateToken(
                savedUser.getId(),
                savedUser.getEmail(),
                savedUser.getRole().name()
        );

        return AuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .userId(savedUser.getId())
                .name(savedUser.getName())
                .email(savedUser.getEmail())
                .role(savedUser.getRole())
                .build();
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found!"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("The password is incorrect!");
        }

        String token = jwtTokenProvider.generateToken(
                user.getId(),
                user.getEmail(),
                user.getRole().name()
        );

        String driverId = null;
        if (user.getRole() == Role.DRIVER) {
            try {
                DriverProfileProvisionResponse profile = driverProfileClient.getProfile(token);
                if (profile != null) {
                    if (profile.id() == null || profile.id().isBlank()) {
                        throw new RestClientException("Driver service returned an invalid driver profile ID.");
                    }
                    driverId = profile.id();
                }
            } catch (RestClientException ex) {
                log.error("Driver profile lookup failed for account {}", user.getId(), ex);
                throw new DriverProfileProvisioningException(
                        "Driver profile is not available for this account. Create it first with "
                                + "POST /api/drivers/me/profile in Driver & Vehicle Service.", ex);
            }
        }

        return AuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .userId(user.getId())
                .driverId(driverId)
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }
}