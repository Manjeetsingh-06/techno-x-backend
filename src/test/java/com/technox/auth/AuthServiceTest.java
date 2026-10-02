package com.technox.auth;

import com.technox.auth.dto.AuthResponse;
import com.technox.auth.dto.LoginRequest;
import com.technox.auth.dto.RefreshTokenRequest;
import com.technox.auth.entity.RefreshToken;
import com.technox.auth.repository.RefreshTokenRepository;
import com.technox.auth.service.AuthService;
import com.technox.common.exception.BusinessException;
import com.technox.common.exception.ErrorCode;
import com.technox.committee.repository.CommitteeMemberRepository;
import com.technox.email.EmailService;
import com.technox.faculty.repository.FacultyRepository;
import com.technox.otp.OtpService;
import com.technox.security.JwtTokenProvider;
import com.technox.student.repository.StudentRepository;
import com.technox.user.entity.Role;
import com.technox.user.entity.RoleType;
import com.technox.user.entity.User;
import com.technox.user.entity.UserStatus;
import com.technox.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private RefreshTokenRepository refreshTokenRepository;
    @Mock
    private StudentRepository studentRepository;
    @Mock
    private FacultyRepository facultyRepository;
    @Mock
    private CommitteeMemberRepository committeeMemberRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtTokenProvider tokenProvider;
    @Mock
    private EmailService emailService;
    @Mock
    private OtpService otpService;

    @InjectMocks
    private AuthService authService;

    private User testUser;
    private Role adminRole;

    @BeforeEach
    void setUp() {
        adminRole = Role.builder().name(RoleType.ADMIN).build();
        testUser = User.builder()
                .name("Admin User")
                .email("admin@technox.test")
                .passwordHash("hashedPassword")
                .status(UserStatus.ACTIVE)
                .enabled(true)
                .roles(Collections.singleton(adminRole))
                .build();
        testUser.setId(1L);
    }

    @Test
    @DisplayName("Should successfully login with valid credentials")
    void testLogin_Success() {
        LoginRequest request = new LoginRequest("admin@technox.test", "AdminPassword123!");

        when(userRepository.findByEmail("admin@technox.test")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("AdminPassword123!", "hashedPassword")).thenReturn(true);
        when(tokenProvider.generateAccessToken(testUser)).thenReturn("mock.jwt.token");
        when(tokenProvider.getExpirationMs()).thenReturn(900000L);
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(i -> i.getArgument(0));

        AuthResponse response = authService.login(request);

        assertNotNull(response);
        assertEquals("mock.jwt.token", response.getToken());
        assertEquals("admin@technox.test", response.getUser().getEmail());
        verify(userRepository).save(testUser);
    }

    @Test
    @DisplayName("Should throw BusinessException when password does not match")
    void testLogin_InvalidPassword() {
        LoginRequest request = new LoginRequest("admin@technox.test", "WrongPassword");

        when(userRepository.findByEmail("admin@technox.test")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("WrongPassword", "hashedPassword")).thenReturn(false);

        BusinessException ex = assertThrows(BusinessException.class, () -> authService.login(request));
        assertEquals(ErrorCode.UNAUTHORIZED, ex.getErrorCode());
    }

    @Test
    @DisplayName("Should throw BusinessException when user does not exist")
    void testLogin_UserNotFound() {
        LoginRequest request = new LoginRequest("unknown@technox.test", "Password123!");

        when(userRepository.findByEmail("unknown@technox.test")).thenReturn(Optional.empty());

        BusinessException ex = assertThrows(BusinessException.class, () -> authService.login(request));
        assertEquals(ErrorCode.UNAUTHORIZED, ex.getErrorCode());
    }

    @Test
    @DisplayName("Should refresh token successfully with valid refresh token")
    void testRefreshToken_Success() {
        RefreshToken token = RefreshToken.builder()
                .user(testUser)
                .token("valid-uuid-token")
                .expiresAt(LocalDateTime.now().plusDays(5))
                .revoked(false)
                .build();

        when(refreshTokenRepository.findByToken("valid-uuid-token")).thenReturn(Optional.of(token));
        when(tokenProvider.generateAccessToken(testUser)).thenReturn("new.access.token");
        when(tokenProvider.getExpirationMs()).thenReturn(900000L);

        AuthResponse response = authService.refreshToken(new RefreshTokenRequest("valid-uuid-token"));

        assertNotNull(response);
        assertEquals("new.access.token", response.getToken());
        assertEquals("valid-uuid-token", response.getRefreshToken());
    }

    @Test
    @DisplayName("Should reject expired or revoked refresh token")
    void testRefreshToken_ExpiredOrRevoked() {
        RefreshToken expiredToken = RefreshToken.builder()
                .user(testUser)
                .token("expired-token")
                .expiresAt(LocalDateTime.now().minusHours(1))
                .revoked(false)
                .build();

        when(refreshTokenRepository.findByToken("expired-token")).thenReturn(Optional.of(expiredToken));

        assertThrows(BusinessException.class, () ->
                authService.refreshToken(new RefreshTokenRequest("expired-token")));
    }
}
