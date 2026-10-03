package com.ridelink.account.services;

import com.ridelink.account.dto.AuthResponse;
import com.ridelink.account.dto.DriverProfileRequest;
import com.ridelink.account.dto.LoginRequest;
import com.ridelink.account.dto.RegisterRequest;
import com.ridelink.account.exceptions.DriverProfileProvisioningException;
import com.ridelink.account.models.Role;
import com.ridelink.account.models.User;
import com.ridelink.account.repositories.UserRepository;
import com.ridelink.account.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final DriverProfileClient driverProfileClient;

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("This email address is already registered!");
        }

        if (request.getRole() == Role.DRIVER) {
            if (request.getLicenseNumber() == null || request.getLicenseNumber().isBlank()) {
                throw new IllegalArgumentException("License number is required for driver registration.");
            }
            if (request.getPhone() == null || request.getPhone().isBlank()) {
                throw new IllegalArgumentException("Phone number is required for driver registration.");
            }
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
        if (savedUser.getRole() == Role.DRIVER) {
            try {
                driverProfileClient.createProfile(new DriverProfileRequest(
                        savedUser.getId(),
                        request.getLicenseNumber(),
                        request.getServiceArea()));
            } catch (DriverProfileProvisioningException exception) {
                try {
                    userRepository.delete(savedUser);
                } catch (RuntimeException rollbackException) {
                    exception.addSuppressed(rollbackException);
                }
                throw exception;
            }
        }

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

        return AuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .userId(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }
}