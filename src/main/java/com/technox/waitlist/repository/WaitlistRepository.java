package com.technox.waitlist.repository;

import com.technox.waitlist.entity.WaitlistEntry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WaitlistRepository extends JpaRepository<WaitlistEntry, Long> {

    Optional<WaitlistEntry> findByEventIdAndStudentId(Long eventId, Long studentId);

    boolean existsByEventIdAndStudentId(Long eventId, Long studentId);

    List<WaitlistEntry> findByEventIdAndStatusOrderByPositionAsc(Long eventId, String status);

    Page<WaitlistEntry> findByEventIdAndStatus(Long eventId, String status, Pageable pageable);

    @Query("SELECT COALESCE(MAX(w.position), 0) FROM WaitlistEntry w WHERE w.event.id = :eventId")
    int findMaxPositionByEventId(@Param("eventId") Long eventId);
}
