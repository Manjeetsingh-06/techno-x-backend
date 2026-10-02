package com.technox.student.service;

import com.technox.common.dto.PagedResponse;
import com.technox.common.exception.BusinessException;
import com.technox.common.exception.ErrorCode;
import com.technox.student.dto.CreateStudentRequest;
import com.technox.student.dto.StudentDto;
import com.technox.student.entity.Student;
import com.technox.student.repository.StudentRepository;
import com.technox.user.entity.Role;
import com.technox.user.entity.RoleType;
import com.technox.user.entity.User;
import com.technox.user.entity.UserStatus;
import com.technox.user.repository.RoleRepository;
import com.technox.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class StudentService {

    private final StudentRepository studentRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public PagedResponse<StudentDto> getStudents(String search, String course, String year, Pageable pageable) {
        Page<Student> page = studentRepository.findWithFilters(search, course, year, pageable);
        List<StudentDto> content = page.getContent().stream().map(this::mapToDto).toList();
        return PagedResponse.of(content, page);
    }

    @Transactional(readOnly = true)
    public StudentDto getStudentById(Long id) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Student not found with ID: " + id));
        return mapToDto(student);
    }

    @Transactional(readOnly = true)
    public StudentDto getStudentByStudentId(String studentId) {
        Student student = studentRepository.findByStudentId(studentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Student not found with student ID: " + studentId));
        return mapToDto(student);
    }

    @Transactional(readOnly = true)
    public StudentDto getStudentByUserId(Long userId) {
        Student student = studentRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Student record not found for user ID: " + userId));
        return mapToDto(student);
    }

    @Transactional
    public StudentDto createStudent(CreateStudentRequest request) {
        if (userRepository.existsByEmail(request.getEmail().trim().toLowerCase())) {
            throw new BusinessException(ErrorCode.CONFLICT, "User already exists with email: " + request.getEmail());
        }
        if (studentRepository.existsByStudentId(request.getStudentId().trim())) {
            throw new BusinessException(ErrorCode.CONFLICT, "Student ID already registered: " + request.getStudentId());
        }

        Role studentRole = roleRepository.findByName(RoleType.STUDENT)
                .orElseThrow(() -> new BusinessException(ErrorCode.INTERNAL_ERROR, "Role STUDENT not configured"));

        String rawPassword = (request.getPassword() != null && !request.getPassword().isBlank())
                ? request.getPassword()
                : "StudentPassword123!";

        User user = User.builder()
                .uuid(UUID.randomUUID().toString())
                .name(request.getName().trim())
                .email(request.getEmail().trim().toLowerCase())
                .mobile(request.getMobile())
                .passwordHash(passwordEncoder.encode(rawPassword))
                .status(UserStatus.ACTIVE)
                .enabled(true)
                .emailVerified(true)
                .roles(Collections.singleton(studentRole))
                .build();

        user = userRepository.save(user);

        Student student = Student.builder()
                .user(user)
                .studentId(request.getStudentId().trim().toUpperCase())
                .fatherName(request.getFatherName() != null ? request.getFatherName().trim() : null)
                .dob(request.getDob() != null ? request.getDob().trim() : null)
                .course(request.getCourse())
                .year(request.getYear())
                .semester(request.getSemester())
                .department(request.getDepartment())
                .bloodGroup(request.getBloodGroup())
                .address(request.getAddress())
                .qrCode("QR-" + request.getStudentId().trim().toUpperCase())
                .build();

        student = studentRepository.save(student);
        log.info("Created new student: {} with user ID: {}", student.getStudentId(), user.getId());
        return mapToDto(student);
    }

    @Transactional
    public StudentDto updateStudentProfile(Long id, StudentDto updateDto) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Student not found with ID: " + id));

        if (updateDto.getName() != null) {
            student.getUser().setName(updateDto.getName().trim());
        }
        if (updateDto.getMobile() != null) {
            student.getUser().setMobile(updateDto.getMobile().trim());
        }
        if (updateDto.getCourse() != null) student.setCourse(updateDto.getCourse());
        if (updateDto.getYear() != null) student.setYear(updateDto.getYear());
        if (updateDto.getSemester() != null) student.setSemester(updateDto.getSemester());
        if (updateDto.getDepartment() != null) student.setDepartment(updateDto.getDepartment());
        if (updateDto.getBloodGroup() != null) student.setBloodGroup(updateDto.getBloodGroup());
        if (updateDto.getAddress() != null) student.setAddress(updateDto.getAddress());
        if (updateDto.getAvatarUrl() != null) student.setAvatarUrl(updateDto.getAvatarUrl());

        student = studentRepository.save(student);
        return mapToDto(student);
    }

    public StudentDto mapToDto(Student student) {
        return StudentDto.builder()
                .id(student.getId())
                .userId(student.getUser().getId())
                .name(student.getUser().getName())
                .email(student.getUser().getEmail())
                .mobile(student.getUser().getMobile())
                .studentId(student.getStudentId())
                .fatherName(student.getFatherName())
                .dob(student.getDob())
                .course(student.getCourse())
                .year(student.getYear())
                .semester(student.getSemester())
                .department(student.getDepartment())
                .bloodGroup(student.getBloodGroup())
                .address(student.getAddress())
                .avatarUrl(student.getAvatarUrl())
                .qrCode(student.getQrCode())
                .createdAt(student.getCreatedAt())
                .build();
    }
}
