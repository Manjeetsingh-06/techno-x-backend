package com.technox.committee.service;

import com.technox.committee.dto.CommitteeMemberDto;
import com.technox.committee.entity.CommitteeMember;
import com.technox.committee.repository.CommitteeMemberRepository;
import com.technox.common.exception.BusinessException;
import com.technox.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import com.technox.committee.dto.CreateCommitteeMemberRequest;
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
public class CommitteeService {

    private final CommitteeMemberRepository committeeMemberRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public CommitteeMemberDto createMember(CreateCommitteeMemberRequest request) {
        String email = request.getEmail().trim().toLowerCase();
        if (userRepository.existsByEmail(email)) {
            throw new BusinessException(ErrorCode.CONFLICT, "User already exists with email: " + email);
        }

        Role committeeRole = roleRepository.findByName(RoleType.MANAGEMENT_COMMITTEE)
                .orElseThrow(() -> new BusinessException(ErrorCode.INTERNAL_ERROR, "Role MANAGEMENT_COMMITTEE not found"));

        String rawPassword = (request.getPassword() != null && !request.getPassword().isBlank())
                ? request.getPassword()
                : "CommitteePassword123!";

        User user = User.builder()
                .uuid(UUID.randomUUID().toString())
                .name(request.getName().trim())
                .email(email)
                .mobile(request.getMobile() != null ? request.getMobile().trim() : null)
                .passwordHash(passwordEncoder.encode(rawPassword))
                .status(UserStatus.ACTIVE)
                .enabled(true)
                .emailVerified(true)
                .roles(Collections.singleton(committeeRole))
                .build();

        user = userRepository.save(user);

        String code = request.getCommitteeCode().trim().toUpperCase();
        String name = request.getCommitteeName();
        if (name == null || name.isBlank()) {
            switch (code) {
                case "KIRAN": name = "Kiran Academic Council"; break;
                case "ABHIVYAKTI": name = "Abhivyakti Cultural Council"; break;
                case "OORJA": name = "Oorja Sports Council"; break;
                case "DARPAN": name = "Darpan Media Council"; break;
                case "SANJEEVANI": name = "Sanjeevani Placement Cell"; break;
                case "SRIJAN": name = "Srijan CSR Council"; break;
                default: name = code + " Committee"; break;
            }
        }

        CommitteeMember member = CommitteeMember.builder()
                .user(user)
                .committeeCode(code)
                .committeeName(name)
                .roleTitle(request.getRoleTitle() != null ? request.getRoleTitle().trim() : "Event Coordinator")
                .department(request.getDepartment() != null ? request.getDepartment().trim() : "Operations")
                .build();

        member = committeeMemberRepository.save(member);
        log.info("Appointed new committee member: {} to {}", member.getUser().getName(), member.getCommitteeCode());
        return mapToDto(member);
    }

    @Transactional(readOnly = true)
    public List<CommitteeMemberDto> getAllMembers() {
        return committeeMemberRepository.findAll().stream().map(this::mapToDto).toList();
    }

    @Transactional(readOnly = true)
    public List<CommitteeMemberDto> getMembersByCommittee(String committeeCode) {
        return committeeMemberRepository.findByCommitteeCode(committeeCode.toUpperCase()).stream()
                .map(this::mapToDto).toList();
    }

    @Transactional(readOnly = true)
    public CommitteeMemberDto getMemberById(Long id) {
        CommitteeMember member = committeeMemberRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Committee member not found with ID: " + id));
        return mapToDto(member);
    }

    @Transactional(readOnly = true)
    public CommitteeMemberDto getMemberByUserId(Long userId) {
        CommitteeMember member = committeeMemberRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Committee member not found for user ID: " + userId));
        return mapToDto(member);
    }

    @Transactional
    public CommitteeMemberDto updateMemberProfile(Long id, CommitteeMemberDto updateDto) {
        CommitteeMember member = committeeMemberRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Committee member not found with ID: " + id));

        if (updateDto.getName() != null) member.getUser().setName(updateDto.getName().trim());
        if (updateDto.getMobile() != null) member.getUser().setMobile(updateDto.getMobile().trim());
        if (updateDto.getCommitteeCode() != null) member.setCommitteeCode(updateDto.getCommitteeCode().toUpperCase());
        if (updateDto.getCommitteeName() != null) member.setCommitteeName(updateDto.getCommitteeName());
        if (updateDto.getRoleTitle() != null) member.setRoleTitle(updateDto.getRoleTitle());
        if (updateDto.getDepartment() != null) member.setDepartment(updateDto.getDepartment());

        member = committeeMemberRepository.save(member);
        return mapToDto(member);
    }

    public CommitteeMemberDto mapToDto(CommitteeMember member) {
        return CommitteeMemberDto.builder()
                .id(member.getId())
                .userId(member.getUser().getId())
                .name(member.getUser().getName())
                .email(member.getUser().getEmail())
                .mobile(member.getUser().getMobile())
                .committeeCode(member.getCommitteeCode())
                .committeeName(member.getCommitteeName())
                .roleTitle(member.getRoleTitle())
                .department(member.getDepartment())
                .createdAt(member.getCreatedAt())
                .build();
    }
}
