package com.technox.attendance.repository;

import com.technox.attendance.entity.Attendance;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Long> {

    Optional<Attendance> findByEventIdAndStudentId(Long eventId, Long studentId);

    Optional<Attendance> findByRegistrationId(Long registrationId);

    boolean existsByEventIdAndStudentId(Long eventId, Long studentId);

    List<Attendance> findByEventId(Long eventId);

    Page<Attendance> findByEventId(Long eventId, Pageable pageable);

    long countByEventId(Long eventId);
}
