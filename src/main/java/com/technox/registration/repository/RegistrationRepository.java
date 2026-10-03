package com.technox.registration.repository;

import com.technox.registration.entity.Registration;
import com.technox.registration.entity.RegistrationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RegistrationRepository extends JpaRepository<Registration, Long> {

    Optional<Registration> findByRegistrationId(String registrationId);

    Optional<Registration> findByQrToken(String qrToken);

    Optional<Registration> findByDigitalPassId(String digitalPassId);

    List<Registration> findByEventId(Long eventId);

    @Query("SELECT r FROM Registration r WHERE r.student.studentId = :studentId")
    List<Registration> findByStudentStudentId(@Param("studentId") String studentId);

    Optional<Registration> findByEventIdAndStudentId(Long eventId, Long studentId);

    boolean existsByEventIdAndStudentId(Long eventId, Long studentId);

    List<Registration> findByStudentId(Long studentId);

    Page<Registration> findByStudentId(Long studentId, Pageable pageable);

    @Query("SELECT r FROM Registration r WHERE " +
           "(:eventId IS NULL OR r.event.id = :eventId) AND " +
           "(:status IS NULL OR r.status = :status) AND " +
           "(:search IS NULL OR LOWER(r.registrationId) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(r.student.studentId) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(r.student.user.name) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Registration> findWithFilters(@Param("eventId") Long eventId,
                                       @Param("status") RegistrationStatus status,
                                       @Param("search") String search,
                                       Pageable pageable);

    long countByEventIdAndStatus(Long eventId, RegistrationStatus status);
}
