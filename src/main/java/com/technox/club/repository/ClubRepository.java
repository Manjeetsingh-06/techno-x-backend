package com.technox.club.repository;

import com.technox.club.entity.Club;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ClubRepository extends JpaRepository<Club, Long> {

    Optional<Club> findByCode(String code);

    boolean existsByCode(String code);
}
