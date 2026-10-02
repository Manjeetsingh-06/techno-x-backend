package com.technox.auth.controller;

import com.technox.auth.dto.*;
import com.technox.auth.service.AuthService;
import com.technox.common.dto.ApiResponse;
import com.technox.user.dto.UserDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Endpoints for user login, JWT refresh, logout, and password recovery")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    @Operation(summary = "Authenticate user and return JWT tokens")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success(response, "Login successful"));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Generate new JWT access token using a valid refresh token")
    public ResponseEntity<ApiResponse<AuthResponse>> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        AuthResponse response = authService.refreshToken(request);
        return ResponseEntity.ok(ApiResponse.success(response, "Token refreshed successfully"));
    }

    @PostMapping("/logout")
    @Operation(summary = "Invalidate refresh token and end session")
    public ResponseEntity<ApiResponse<Void>> logout(@RequestBody(required = false) RefreshTokenRequest request) {
        if (request != null) {
            authService.logout(request.getRefreshToken());
        }
        return ResponseEntity.ok(ApiResponse.success(null, "Logged out successfully"));
    }

    @PostMapping("/forgot-password")
    @Operation(summary = "Request password reset OTP for account")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        authService.forgotPassword(request);
        return ResponseEntity.ok(ApiResponse.success(null, "If an account exists, a reset OTP has been sent."));
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Reset password using OTP")
    public ResponseEntity<ApiResponse<Void>> resetPassword(@Valid @RequestBody PasswordResetRequest request) {
        authService.resetPassword(request);
        return ResponseEntity.ok(ApiResponse.success(null, "Password has been successfully updated."));
    }

    @PostMapping("/send-otp")
    @Operation(summary = "Send verification code to student email or mobile")
    public ResponseEntity<ApiResponse<String>> sendOtp(@Valid @RequestBody SendOtpRequest request) {
        String code = authService.sendOtp(request);
        return ResponseEntity.ok(ApiResponse.success(code, "Verification code sent to " + request.getTarget()));
    }

    @PostMapping("/verify-otp")
    @Operation(summary = "Verify OTP code for email or mobile")
    public ResponseEntity<ApiResponse<Boolean>> verifyOtp(@Valid @RequestBody VerifyOtpRequest request) {
        boolean valid = authService.verifyOtp(request);
        if (!valid) {
            return ResponseEntity.badRequest().body(ApiResponse.success(false, "Invalid or expired verification code"));
        }
        return ResponseEntity.ok(ApiResponse.success(true, "Verification successful"));
    }

    @PostMapping("/register")
    @Operation(summary = "Student self-registration portal with identity verification")
    public ResponseEntity<ApiResponse<AuthResponse>> registerStudent(@Valid @RequestBody StudentRegisterRequest request) {
        AuthResponse response = authService.registerStudent(request);
        return ResponseEntity.ok(ApiResponse.success(response, "Student registration completed successfully"));
    }

    @GetMapping("/me")
    @Operation(summary = "Get currently authenticated user profile")
    public ResponseEntity<ApiResponse<UserDto>> getCurrentUser(@AuthenticationPrincipal UserDetails userDetails) {
        UserDto userDto = authService.getCurrentUser(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(userDto));
    }
}
