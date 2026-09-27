package com.ridelink.account.controllers;

import com.ridelink.account.dto.UserResponse;
import com.ridelink.account.models.User;
import com.ridelink.account.services.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "User Management", description = "පරිශීලක ගිණුම් විස්තර ලබා ගැනීමේ Endpoints")
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    @Operation(
            summary = "ලොග් වී සිටින පරිශීලකයාගේ විස්තර ලබා ගැනීම",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<UserResponse> getCurrentUser(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(userService.getUserProfile(user.getId()));
    }
}