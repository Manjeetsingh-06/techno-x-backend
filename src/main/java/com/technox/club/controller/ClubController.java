package com.technox.club.controller;

import com.technox.club.dto.ClubDto;
import com.technox.club.service.ClubService;
import com.technox.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/clubs")
@RequiredArgsConstructor
@Tag(name = "Clubs", description = "Endpoints for college clubs and student societies")
public class ClubController {

    private final ClubService clubService;

    @GetMapping
    @Operation(summary = "Get list of all student clubs")
    public ResponseEntity<ApiResponse<List<ClubDto>>> getAllClubs() {
        return ResponseEntity.ok(ApiResponse.success(clubService.getAllClubs()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get club by ID")
    public ResponseEntity<ApiResponse<ClubDto>> getClubById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(clubService.getClubById(id)));
    }

    @GetMapping("/code/{code}")
    @Operation(summary = "Get club by code (e.g. CODING_CLUB)")
    public ResponseEntity<ApiResponse<ClubDto>> getClubByCode(@PathVariable String code) {
        return ResponseEntity.ok(ApiResponse.success(clubService.getClubByCode(code)));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a new club")
    public ResponseEntity<ApiResponse<ClubDto>> createClub(@RequestBody ClubDto dto) {
        ClubDto created = clubService.createClub(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(created, "Club created"));
    }
}
