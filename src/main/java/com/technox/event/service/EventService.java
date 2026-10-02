package com.technox.event.service;

import com.technox.category.dto.CategoryDto;
import com.technox.category.entity.Category;
import com.technox.category.repository.CategoryRepository;
import com.technox.club.dto.ClubDto;
import com.technox.club.entity.Club;
import com.technox.club.repository.ClubRepository;
import com.technox.common.dto.PagedResponse;
import com.technox.common.exception.BusinessException;
import com.technox.common.exception.ErrorCode;
import com.technox.event.dto.CreateEventRequest;
import com.technox.event.dto.EventDto;
import com.technox.event.dto.UpdateEventRequest;
import com.technox.event.entity.Event;
import com.technox.event.entity.EventStatus;
import com.technox.event.repository.EventRepository;
import com.technox.user.entity.User;
import com.technox.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;
    private final CategoryRepository categoryRepository;
    private final ClubRepository clubRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public PagedResponse<EventDto> getEvents(EventStatus status, Long categoryId, String committeeCode, String search, Pageable pageable) {
        Page<Event> page = eventRepository.findWithFilters(status, categoryId, committeeCode, search, pageable);
        List<EventDto> dtos = page.getContent().stream().map(this::mapToDto).toList();
        return PagedResponse.of(dtos, page);
    }

    @Transactional(readOnly = true)
    public EventDto getEventById(Long id) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Event not found with ID: " + id));
        return mapToDto(event);
    }

    @Transactional(readOnly = true)
    public EventDto getEventByUuid(String uuid) {
        Event event = eventRepository.findByUuid(uuid)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Event not found with UUID: " + uuid));
        return mapToDto(event);
    }

    @Transactional
    public EventDto createEvent(CreateEventRequest request, Long userId) {
        User creator = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "User not found with ID: " + userId));

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Category not found with ID: " + request.getCategoryId()));

        Club club = null;
        if (request.getClubId() != null) {
            club = clubRepository.findById(request.getClubId()).orElse(null);
        }

        String slug = request.getTitle().toLowerCase().replaceAll("[^a-z0-9]+", "-").replaceAll("^-|-$", "");

        Event event = Event.builder()
                .uuid(UUID.randomUUID().toString())
                .title(request.getTitle().trim())
                .slug(slug)
                .description(request.getDescription())
                .category(category)
                .club(club)
                .committeeCode(request.getCommitteeCode() != null ? request.getCommitteeCode().toUpperCase() : null)
                .organizer(request.getOrganizer())
                .bannerUrl(request.getBannerUrl())
                .eventDate(request.getEventDate())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .venue(request.getVenue())
                .capacity(request.getCapacity())
                .registeredCount(0)
                .waitlistCount(0)
                .registrationDeadline(request.getRegistrationDeadline())
                .eligibility(request.getEligibility())
                .rules(request.getRules())
                .requirements(request.getRequirements())
                .status(EventStatus.DRAFT)
                .createdBy(creator)
                .facultyCoordinator(request.getFacultyCoordinator())
                .committeeCoordinator(request.getCommitteeCoordinator())
                .build();

        event = eventRepository.save(event);
        log.info("Created event: {} (ID: {}) by user: {}", event.getTitle(), event.getId(), creator.getEmail());
        return mapToDto(event);
    }

    @Transactional
    public EventDto updateEvent(Long id, UpdateEventRequest request) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Event not found with ID: " + id));

        if (request.getTitle() != null) event.setTitle(request.getTitle().trim());
        if (request.getDescription() != null) event.setDescription(request.getDescription());
        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Category not found"));
            event.setCategory(category);
        }
        if (request.getClubId() != null) {
            Club club = clubRepository.findById(request.getClubId()).orElse(null);
            event.setClub(club);
        }
        if (request.getCommitteeCode() != null) event.setCommitteeCode(request.getCommitteeCode().toUpperCase());
        if (request.getOrganizer() != null) event.setOrganizer(request.getOrganizer());
        if (request.getBannerUrl() != null) event.setBannerUrl(request.getBannerUrl());
        if (request.getEventDate() != null) event.setEventDate(request.getEventDate());
        if (request.getStartTime() != null) event.setStartTime(request.getStartTime());
        if (request.getEndTime() != null) event.setEndTime(request.getEndTime());
        if (request.getVenue() != null) event.setVenue(request.getVenue());
        if (request.getCapacity() != null) event.setCapacity(request.getCapacity());
        if (request.getRegistrationDeadline() != null) event.setRegistrationDeadline(request.getRegistrationDeadline());
        if (request.getEligibility() != null) event.setEligibility(request.getEligibility());
        if (request.getRules() != null) event.setRules(request.getRules());
        if (request.getRequirements() != null) event.setRequirements(request.getRequirements());
        if (request.getStatus() != null) event.setStatus(request.getStatus());
        if (request.getFacultyCoordinator() != null) event.setFacultyCoordinator(request.getFacultyCoordinator());
        if (request.getCommitteeCoordinator() != null) event.setCommitteeCoordinator(request.getCommitteeCoordinator());

        event = eventRepository.save(event);
        return mapToDto(event);
    }

    @Transactional
    public EventDto approveEvent(Long eventId, Long approverUserId, boolean approved, String reason) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Event not found with ID: " + eventId));

        if (approved) {
            event.setStatus(EventStatus.APPROVED);
        } else {
            event.setStatus(EventStatus.REJECTED);
        }
        event.setApprovedByUserId(approverUserId);
        event.setApprovedAt(LocalDateTime.now());
        event.setApprovalReason(reason);

        event = eventRepository.save(event);
        log.info("Event {} approval status updated to: {}", event.getId(), event.getStatus());
        return mapToDto(event);
    }

    @Transactional
    public EventDto publishEvent(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Event not found with ID: " + eventId));

        event.setStatus(EventStatus.PUBLISHED);
        event = eventRepository.save(event);
        log.info("Event {} published successfully", event.getId());
        return mapToDto(event);
    }

    @Transactional
    public EventDto cancelEvent(Long eventId, String reason) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Event not found with ID: " + eventId));

        event.setStatus(EventStatus.CANCELLED);
        event.setApprovalReason(reason);
        event = eventRepository.save(event);
        log.info("Event {} cancelled. Reason: {}", event.getId(), reason);
        return mapToDto(event);
    }

    public EventDto mapToDto(Event event) {
        CategoryDto categoryDto = null;
        if (event.getCategory() != null) {
            categoryDto = CategoryDto.builder()
                    .id(event.getCategory().getId())
                    .name(event.getCategory().getName())
                    .description(event.getCategory().getDescription())
                    .colorCode(event.getCategory().getColorCode())
                    .build();
        }

        ClubDto clubDto = null;
        if (event.getClub() != null) {
            clubDto = ClubDto.builder()
                    .id(event.getClub().getId())
                    .code(event.getClub().getCode())
                    .name(event.getClub().getName())
                    .tagline(event.getClub().getTagline())
                    .description(event.getClub().getDescription())
                    .logoUrl(event.getClub().getLogoUrl())
                    .facultyCoordinator(event.getClub().getFacultyCoordinator())
                    .active(event.getClub().isActive())
                    .build();
        }

        return EventDto.builder()
                .id(event.getId())
                .uuid(event.getUuid())
                .title(event.getTitle())
                .slug(event.getSlug())
                .description(event.getDescription())
                .category(categoryDto)
                .club(clubDto)
                .committeeCode(event.getCommitteeCode())
                .organizer(event.getOrganizer())
                .bannerUrl(event.getBannerUrl())
                .eventDate(event.getEventDate())
                .startTime(event.getStartTime())
                .endTime(event.getEndTime())
                .venue(event.getVenue())
                .capacity(event.getCapacity())
                .registeredCount(event.getRegisteredCount())
                .waitlistCount(event.getWaitlistCount())
                .registrationDeadline(event.getRegistrationDeadline())
                .eligibility(event.getEligibility())
                .rules(event.getRules())
                .requirements(event.getRequirements())
                .status(event.getStatus())
                .createdByUserId(event.getCreatedBy() != null ? event.getCreatedBy().getId() : null)
                .creatorName(event.getCreatedBy() != null ? event.getCreatedBy().getName() : null)
                .facultyCoordinator(event.getFacultyCoordinator())
                .committeeCoordinator(event.getCommitteeCoordinator())
                .approvedByUserId(event.getApprovedByUserId())
                .approvedAt(event.getApprovedAt())
                .approvalReason(event.getApprovalReason())
                .createdAt(event.getCreatedAt())
                .build();
    }
}
