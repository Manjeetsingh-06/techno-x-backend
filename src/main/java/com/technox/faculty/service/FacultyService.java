package com.technox.faculty.service;

import com.technox.common.exception.BusinessException;
import com.technox.common.exception.ErrorCode;
import com.technox.faculty.dto.FacultyDto;
import com.technox.faculty.entity.Faculty;
import com.technox.faculty.repository.FacultyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import com.technox.faculty.dto.CreateFacultyRequest;
import com.technox.user.entity.Role;
import com.technox.user.entity.RoleType;
import com.technox.user.entity.User;
import com.technox.user.entity.UserStatus;
import com.technox.user.repository.RoleRepository;
import com.technox.user.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.Collections;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class FacultyService {

    private final FacultyRepository facultyRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public FacultyDto createFaculty(CreateFacultyRequest request) {
        String email = request.getEmail().trim().toLowerCase();
        if (userRepository.existsByEmail(email)) {
            throw new BusinessException(ErrorCode.CONFLICT, "User already exists with email: " + email);
        }

        Role facultyRole = roleRepository.findByName(RoleType.FACULTY)
                .orElseThrow(() -> new BusinessException(ErrorCode.INTERNAL_ERROR, "Role FACULTY not found"));

        String rawPassword = (request.getPassword() != null && !request.getPassword().isBlank())
                ? request.getPassword()
                : "FacultyPassword123!";

        User user = User.builder()
                .uuid(UUID.randomUUID().toString())
                .name(request.getName().trim())
                .email(email)
                .mobile(request.getMobile() != null ? request.getMobile().trim() : null)
                .passwordHash(passwordEncoder.encode(rawPassword))
                .status(UserStatus.ACTIVE)
                .enabled(true)
                .emailVerified(true)
                .roles(Collections.singleton(facultyRole))
                .build();

        user = userRepository.save(user);

        String facultyCode = (request.getFacultyCode() != null && !request.getFacultyCode().isBlank())
                ? request.getFacultyCode().trim().toUpperCase()
                : "FAC-" + System.currentTimeMillis() % 10000;

        Faculty faculty = Faculty.builder()
                .user(user)
                .facultyCode(facultyCode)
                .designation(request.getDesignation() != null ? request.getDesignation() : "Assistant Professor")
                .department(request.getDepartment().trim())
                .specialization(request.getSpecialization())
                .officeRoom(request.getOfficeRoom())
                .build();

        faculty = facultyRepository.save(faculty);
        log.info("Appointed new faculty: {} with code {}", faculty.getUser().getName(), faculty.getFacultyCode());
        return mapToDto(faculty);
    }

    @Transactional(readOnly = true)
    public List<FacultyDto> getAllFaculty() {
        return facultyRepository.findAll().stream().map(this::mapToDto).toList();
    }

    @Transactional(readOnly = true)
    public FacultyDto getFacultyById(Long id) {
        Faculty faculty = facultyRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Faculty not found with ID: " + id));
        return mapToDto(faculty);
    }

    @Transactional(readOnly = true)
    public FacultyDto getFacultyByUserId(Long userId) {
        Faculty faculty = facultyRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Faculty record not found for user ID: " + userId));
        return mapToDto(faculty);
    }

    @Transactional
    public FacultyDto updateFacultyProfile(Long id, FacultyDto updateDto) {
        Faculty faculty = facultyRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Faculty not found with ID: " + id));

        if (updateDto.getName() != null) faculty.getUser().setName(updateDto.getName().trim());
        if (updateDto.getMobile() != null) faculty.getUser().setMobile(updateDto.getMobile().trim());
        if (updateDto.getDesignation() != null) faculty.setDesignation(updateDto.getDesignation());
        if (updateDto.getDepartment() != null) faculty.setDepartment(updateDto.getDepartment());
        if (updateDto.getSpecialization() != null) faculty.setSpecialization(updateDto.getSpecialization());
        if (updateDto.getOfficeRoom() != null) faculty.setOfficeRoom(updateDto.getOfficeRoom());
        if (updateDto.getAvatarUrl() != null) faculty.setAvatarUrl(updateDto.getAvatarUrl());

        faculty = facultyRepository.save(faculty);
        return mapToDto(faculty);
    }

    public FacultyDto mapToDto(Faculty faculty) {
        return FacultyDto.builder()
                .id(faculty.getId())
                .userId(faculty.getUser().getId())
                .name(faculty.getUser().getName())
                .email(faculty.getUser().getEmail())
                .mobile(faculty.getUser().getMobile())
                .facultyCode(faculty.getFacultyCode())
                .designation(faculty.getDesignation())
                .department(faculty.getDepartment())
                .specialization(faculty.getSpecialization())
                .officeRoom(faculty.getOfficeRoom())
                .avatarUrl(faculty.getAvatarUrl())
                .createdAt(faculty.getCreatedAt())
                .build();
    }
}
