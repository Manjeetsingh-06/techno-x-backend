package com.technox.student.repository;

import com.technox.student.entity.Student;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {

    Optional<Student> findByStudentId(String studentId);

    Optional<Student> findByUserId(Long userId);

    boolean existsByStudentId(String studentId);

    @Query("SELECT s FROM Student s WHERE " +
           "(:search IS NULL OR LOWER(s.studentId) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(s.user.name) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(s.user.email) LIKE LOWER(CONCAT('%', :search, '%'))) AND " +
           "(:course IS NULL OR s.course = :course) AND " +
           "(:year IS NULL OR s.year = :year)")
    Page<Student> findWithFilters(@Param("search") String search,
                                  @Param("course") String course,
                                  @Param("year") String year,
                                  Pageable pageable);
}
