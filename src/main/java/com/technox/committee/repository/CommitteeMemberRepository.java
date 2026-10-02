package com.technox.committee.repository;

import com.technox.committee.entity.CommitteeMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CommitteeMemberRepository extends JpaRepository<CommitteeMember, Long> {

    Optional<CommitteeMember> findByUserId(Long userId);

    List<CommitteeMember> findByCommitteeCode(String committeeCode);
}
