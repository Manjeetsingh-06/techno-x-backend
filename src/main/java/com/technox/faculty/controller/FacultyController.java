package com.technox.faculty.controller;

import com.technox.common.dto.ApiResponse;
import com.technox.faculty.dto.FacultyDto;
import com.technox.faculty.service.FacultyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import com.technox.faculty.dto.CreateFacultyRequest;
import org.springframework.http.HttpStatus;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/faculty")
@RequiredArgsConstructor
@Tag(name = "Faculty", description = "Endpoints for faculty profiles and directories")
public class FacultyController {

    private final FacultyService facultyService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Appoint a new faculty member (Admin only)")
    public ResponseEntity<ApiResponse<FacultyDto>> createFaculty(@Valid @RequestBody CreateFacultyRequest request) {
        FacultyDto dto = facultyService.createFaculty(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(dto, "Faculty appointed successfully"));
    }

    @GetMapping
    @Operation(summary = "Get list of all faculty members")
    public ResponseEntity<ApiResponse<List<FacultyDto>>> getAllFaculty() {
        List<FacultyDto> list = facultyService.getAllFaculty();
        return ResponseEntity.ok(ApiResponse.success(list));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get faculty member by ID")
    public ResponseEntity<ApiResponse<FacultyDto>> getFacultyById(@PathVariable Long id) {
        FacultyDto dto = facultyService.getFacultyById(id);
        return ResponseEntity.ok(ApiResponse.success(dto));
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get faculty profile by User ID")
    public ResponseEntity<ApiResponse<FacultyDto>> getFacultyByUserId(@PathVariable Long userId) {
        FacultyDto dto = facultyService.getFacultyByUserId(userId);
        return ResponseEntity.ok(ApiResponse.success(dto));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'FACULTY')")
    @Operation(summary = "Update faculty profile")
    public ResponseEntity<ApiResponse<FacultyDto>> updateFaculty(
            @PathVariable Long id,
            @RequestBody FacultyDto updateDto
    ) {
        FacultyDto updated = facultyService.updateFacultyProfile(id, updateDto);
        return ResponseEntity.ok(ApiResponse.success(updated, "Faculty profile updated successfully"));
    }
}
