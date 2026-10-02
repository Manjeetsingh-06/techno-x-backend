package com.technox.committee.controller;

import com.technox.committee.dto.CommitteeMemberDto;
import com.technox.committee.service.CommitteeService;
import com.technox.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import com.technox.committee.dto.CreateCommitteeMemberRequest;
import org.springframework.http.HttpStatus;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/committees")
@RequiredArgsConstructor
@Tag(name = "Committees", description = "Endpoints for college committee members and rosters")
public class CommitteeController {

    private final CommitteeService committeeService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Appoint a new management committee member (Admin only)")
    public ResponseEntity<ApiResponse<CommitteeMemberDto>> createMember(@Valid @RequestBody CreateCommitteeMemberRequest request) {
        CommitteeMemberDto dto = committeeService.createMember(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(dto, "Committee member appointed successfully"));
    }

    @GetMapping
    @Operation(summary = "Get list of all committee members across all 6 committees")
    public ResponseEntity<ApiResponse<List<CommitteeMemberDto>>> getAllMembers() {
        List<CommitteeMemberDto> list = committeeService.getAllMembers();
        return ResponseEntity.ok(ApiResponse.success(list));
    }

    @GetMapping("/code/{committeeCode}")
    @Operation(summary = "Get members of a specific committee (e.g. ABHIVYAKTI, KIRAN, OORJA)")
    public ResponseEntity<ApiResponse<List<CommitteeMemberDto>>> getMembersByCommittee(@PathVariable String committeeCode) {
        List<CommitteeMemberDto> list = committeeService.getMembersByCommittee(committeeCode);
        return ResponseEntity.ok(ApiResponse.success(list));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get committee member details by ID")
    public ResponseEntity<ApiResponse<CommitteeMemberDto>> getMemberById(@PathVariable Long id) {
        CommitteeMemberDto dto = committeeService.getMemberById(id);
        return ResponseEntity.ok(ApiResponse.success(dto));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGEMENT_COMMITTEE')")
    @Operation(summary = "Update committee member profile")
    public ResponseEntity<ApiResponse<CommitteeMemberDto>> updateMember(
            @PathVariable Long id,
            @RequestBody CommitteeMemberDto updateDto
    ) {
        CommitteeMemberDto updated = committeeService.updateMemberProfile(id, updateDto);
        return ResponseEntity.ok(ApiResponse.success(updated, "Committee member profile updated successfully"));
    }
}
