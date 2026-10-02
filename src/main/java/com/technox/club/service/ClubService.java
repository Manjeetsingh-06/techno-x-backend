package com.technox.club.service;

import com.technox.club.dto.ClubDto;
import com.technox.club.entity.Club;
import com.technox.club.repository.ClubRepository;
import com.technox.common.exception.BusinessException;
import com.technox.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ClubService {

    private final ClubRepository clubRepository;

    @Transactional(readOnly = true)
    public List<ClubDto> getAllClubs() {
        return clubRepository.findAll().stream().map(this::mapToDto).toList();
    }

    @Transactional(readOnly = true)
    public ClubDto getClubById(Long id) {
        Club club = clubRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Club not found with ID: " + id));
        return mapToDto(club);
    }

    @Transactional(readOnly = true)
    public ClubDto getClubByCode(String code) {
        Club club = clubRepository.findByCode(code.toUpperCase())
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Club not found with code: " + code));
        return mapToDto(club);
    }

    @Transactional
    public ClubDto createClub(ClubDto dto) {
        if (clubRepository.existsByCode(dto.getCode().trim().toUpperCase())) {
            throw new BusinessException(ErrorCode.CONFLICT, "Club code already exists: " + dto.getCode());
        }

        Club club = Club.builder()
                .code(dto.getCode().trim().toUpperCase())
                .name(dto.getName().trim())
                .tagline(dto.getTagline())
                .description(dto.getDescription())
                .logoUrl(dto.getLogoUrl())
                .facultyCoordinator(dto.getFacultyCoordinator())
                .active(true)
                .build();

        club = clubRepository.save(club);
        return mapToDto(club);
    }

    public ClubDto mapToDto(Club club) {
        return ClubDto.builder()
                .id(club.getId())
                .code(club.getCode())
                .name(club.getName())
                .tagline(club.getTagline())
                .description(club.getDescription())
                .logoUrl(club.getLogoUrl())
                .facultyCoordinator(club.getFacultyCoordinator())
                .active(club.isActive())
                .build();
    }
}
