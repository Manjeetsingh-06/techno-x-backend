package com.technox.faculty.repository;

import com.technox.faculty.entity.Faculty;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FacultyRepository extends JpaRepository<Faculty, Long> {

    Optional<Faculty> findByFacultyCode(String facultyCode);

    Optional<Faculty> findByUserId(Long userId);

    Page<Faculty> findByDepartment(String department, Pageable pageable);
}
