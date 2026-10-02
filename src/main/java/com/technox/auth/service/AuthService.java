package com.technox.auth.service;

import com.technox.auth.dto.*;
import com.technox.auth.entity.RefreshToken;
import com.technox.auth.repository.RefreshTokenRepository;
import com.technox.committee.repository.CommitteeMemberRepository;
import com.technox.common.exception.BusinessException;
import com.technox.common.exception.ErrorCode;
import com.technox.email.EmailService;
import com.technox.faculty.repository.FacultyRepository;
import com.technox.otp.OtpService;
import com.technox.security.JwtTokenProvider;
import com.technox.student.repository.StudentRepository;
import com.technox.user.dto.UserDto;
import com.technox.user.entity.User;
import com.technox.user.entity.UserStatus;
import com.technox.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.stream.Collectors;

import com.technox.student.entity.Student;
import com.technox.user.entity.Role;
import com.technox.user.entity.RoleType;
import com.technox.user.repository.RoleRepository;
import java.util.Collections;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final StudentRepository studentRepository;
    private final FacultyRepository facultyRepository;
    private final CommitteeMemberRepository committeeMemberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    private final EmailService emailService;
    private final OtpService otpService;

    @Transactional
    public AuthResponse login(LoginRequest request) {
        String identifier = request.getEmail().trim();
        User user = userRepository.findByEmail(identifier.toLowerCase())
                .orElseGet(() -> studentRepository.findByStudentId(identifier.toUpperCase())
                        .map(com.technox.student.entity.Student::getUser)
                        .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED, "Invalid credentials or user not found")));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            log.warn("Failed login attempt for identifier: {}", identifier);
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "Invalid email or password");
        }

        if (user.getStatus() != UserStatus.ACTIVE || !user.isEnabled()) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "Account is not active or has been disabled");
        }

        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);

        String accessToken = tokenProvider.generateAccessToken(user);
        RefreshToken refreshToken = createRefreshToken(user);

        return AuthResponse.builder()
                .token(accessToken)
                .refreshToken(refreshToken.getToken())
                .tokenType("Bearer")
                .expiresIn(tokenProvider.getExpirationMs())
                .user(mapToUserDto(user))
                .build();
    }

    @Transactional
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(request.getRefreshToken())
                .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED, "Invalid refresh token"));

        if (refreshToken.isRevoked() || refreshToken.isExpired()) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "Refresh token has expired or is revoked");
        }

        User user = refreshToken.getUser();
        String newAccessToken = tokenProvider.generateAccessToken(user);

        return AuthResponse.builder()
                .token(newAccessToken)
                .refreshToken(refreshToken.getToken())
                .tokenType("Bearer")
                .expiresIn(tokenProvider.getExpirationMs())
                .user(mapToUserDto(user))
                .build();
    }

    @Transactional
    public void logout(String refreshTokenString) {
        if (refreshTokenString != null && !refreshTokenString.isBlank()) {
            refreshTokenRepository.findByToken(refreshTokenString).ifPresent(token -> {
                token.setRevoked(true);
                refreshTokenRepository.save(token);
            });
        }
    }

    public void forgotPassword(ForgotPasswordRequest request) {
        userRepository.findByEmail(request.getEmail().trim().toLowerCase()).ifPresent(user -> {
            String mobileOrEmail = user.getMobile() != null ? user.getMobile() : user.getEmail();
            String otp = otpService.generateOtp(mobileOrEmail);
            emailService.sendPasswordResetOtp(user.getEmail(), user.getName(), otp);
            log.info("Sent password reset OTP to: {}", user.getEmail());
        });
    }

    @Transactional
    public void resetPassword(PasswordResetRequest request) {
        User user = userRepository.findByEmail(request.getEmail().trim().toLowerCase())
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "User not found"));

        String identifier = user.getMobile() != null ? user.getMobile() : user.getEmail();
        boolean verified = otpService.verifyOtp(identifier, request.getOtp());
        if (!verified) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "Invalid or expired OTP");
        }

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        // Invalidate previous refresh tokens
        refreshTokenRepository.revokeAllUserTokens(user.getId());
        log.info("Password successfully reset for user: {}", user.getEmail());
    }

    @Transactional(readOnly = true)
    public UserDto getCurrentUser(String email) {
        User user = userRepository.findByEmail(email.trim().toLowerCase())
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "User not found"));
        return mapToUserDto(user);
    }

    public UserDto mapToUserDto(User user) {
        UserDto dto = UserDto.builder()
                .id(user.getId())
                .uuid(user.getUuid())
                .name(user.getName())
                .email(user.getEmail())
                .mobile(user.getMobile())
                .status(user.getStatus())
                .enabled(user.isEnabled())
                .emailVerified(user.isEmailVerified())
                .mobileVerified(user.isMobileVerified())
                .lastLoginAt(user.getLastLoginAt())
                .roles(user.getRoles().stream().map(r -> r.getName().name()).collect(Collectors.toSet()))
                .permissions(user.getRoles().stream()
                        .flatMap(r -> r.getPermissions().stream())
                        .map(p -> p.getName().name())
                        .collect(Collectors.toSet()))
                .build();

        studentRepository.findByUserId(user.getId()).ifPresent(s -> {
            dto.setStudentId(s.getStudentId());
            dto.setCourse(s.getCourse());
            dto.setYear(s.getYear());
            dto.setSemester(s.getSemester());
            dto.setDepartment(s.getDepartment());
            dto.setAvatarUrl(s.getAvatarUrl());
            dto.setQrCode(s.getQrCode());
        });

        facultyRepository.findByUserId(user.getId()).ifPresent(f -> {
            dto.setDepartment(f.getDepartment());
            dto.setDesignation(f.getDesignation());
            dto.setAvatarUrl(f.getAvatarUrl());
        });

        committeeMemberRepository.findByUserId(user.getId()).ifPresent(c -> {
            dto.setCommitteeCode(c.getCommitteeCode());
            dto.setCommitteeName(c.getCommitteeName());
            dto.setRoleTitle(c.getRoleTitle());
            dto.setDepartment(c.getDepartment());
        });

        return dto;
    }

    private RefreshToken createRefreshToken(User user) {
        RefreshToken token = RefreshToken.builder()
                .user(user)
                .token(UUID.randomUUID().toString())
                .expiresAt(LocalDateTime.now().plusDays(7))
                .revoked(false)
                .build();
        return refreshTokenRepository.save(token);
    }

    public String sendOtp(SendOtpRequest request) {
        String target = request.getTarget().trim();
        String otp = otpService.generateOtp(target);
        log.info("[OTP SERVICE] Verification code {} generated for {}", otp, target);
        return otp;
    }

    public boolean verifyOtp(VerifyOtpRequest request) {
        return otpService.verifyOtp(request.getTarget().trim(), request.getOtp().trim());
    }

    @Transactional
    public AuthResponse registerStudent(StudentRegisterRequest request) {
        String email = request.getEmail().trim().toLowerCase();
        String studentId = request.getStudentId().trim().toUpperCase();

        if (userRepository.existsByEmail(email)) {
            throw new BusinessException(ErrorCode.CONFLICT, "An account with email " + email + " already exists.");
        }
        if (studentRepository.existsByStudentId(studentId)) {
            throw new BusinessException(ErrorCode.CONFLICT, "Student ID " + studentId + " is already registered.");
        }

        Role studentRole = roleRepository.findByName(RoleType.STUDENT)
                .orElseThrow(() -> new BusinessException(ErrorCode.INTERNAL_ERROR, "Role STUDENT not found"));

        boolean emailVerified = "EMAIL".equalsIgnoreCase(request.getVerificationType());
        boolean mobileVerified = "MOBILE".equalsIgnoreCase(request.getVerificationType());

        User user = User.builder()
                .uuid(UUID.randomUUID().toString())
                .name(request.getName().trim())
                .email(email)
                .mobile(request.getMobile() != null ? request.getMobile().trim() : null)
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .status(UserStatus.ACTIVE)
                .enabled(true)
                .emailVerified(emailVerified)
                .mobileVerified(mobileVerified)
                .lastLoginAt(LocalDateTime.now())
                .roles(Collections.singleton(studentRole))
                .build();

        user = userRepository.save(user);

        Student student = Student.builder()
                .user(user)
                .studentId(studentId)
                .fatherName(request.getFatherName() != null ? request.getFatherName().trim() : null)
                .dob(request.getDob() != null ? request.getDob().trim() : null)
                .course(request.getCourse().trim())
                .year(request.getYear().trim())
                .qrCode("QR-" + studentId)
                .build();

        studentRepository.save(student);
        log.info("Self-registered new student: {} with user ID: {}", studentId, user.getId());

        String accessToken = tokenProvider.generateAccessToken(user);
        RefreshToken refreshToken = createRefreshToken(user);

        UserDto userDto = mapToUserDto(user);

        return AuthResponse.builder()
                .token(accessToken)
                .refreshToken(refreshToken.getToken())
                .tokenType("Bearer")
                .expiresIn(tokenProvider.getExpirationMs())
                .user(userDto)
                .build();
    }
}
