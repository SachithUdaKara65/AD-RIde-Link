package com.ridelink.drivervehicle.controller;

import com.ridelink.drivervehicle.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
public class AuthTestController {

    private final JwtUtil jwtUtil;

    // Temporary endpoint to generate test tokens
    @GetMapping("/token")
    public Map<String, String> generateToken(
            @RequestParam(defaultValue = "driver1") String username,
            @RequestParam(defaultValue = "DRIVER") String role) {

        String token = jwtUtil.generateToken(username, role);
        return Map.of(
                "token", token,
                "type", "Bearer");
    }
}