package com.technox.event.repository;

import com.technox.event.entity.Event;
import com.technox.event.entity.EventStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {

    Optional<Event> findByUuid(String uuid);

    // Concurrency Safety: PESSIMISTIC_WRITE lock prevents capacity race conditions during registration
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT e FROM Event e WHERE e.id = :id")
    Optional<Event> findByIdWithLock(@Param("id") Long id);

    @Query("SELECT e FROM Event e WHERE " +
           "(:status IS NULL OR e.status = :status) AND " +
           "(:categoryId IS NULL OR e.category.id = :categoryId) AND " +
           "(:committeeCode IS NULL OR e.committeeCode = :committeeCode) AND " +
           "(:search IS NULL OR LOWER(e.title) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(e.description) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(e.venue) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Event> findWithFilters(@Param("status") EventStatus status,
                                @Param("categoryId") Long categoryId,
                                @Param("committeeCode") String committeeCode,
                                @Param("search") String search,
                                Pageable pageable);

    long countByStatus(EventStatus status);

    List<Event> findByEventDateAndStatusIn(LocalDate date, List<EventStatus> statuses);

    @Query("SELECT e FROM Event e WHERE e.createdAt >= :since AND e.status = :status")
    List<Event> findRecentlyPublished(@Param("since") LocalDateTime since, @Param("status") EventStatus status);
}
