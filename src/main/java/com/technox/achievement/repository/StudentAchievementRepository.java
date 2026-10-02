package com.technox.achievement.repository;

import com.technox.achievement.entity.StudentAchievement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StudentAchievementRepository extends JpaRepository<StudentAchievement, Long> {

    List<StudentAchievement> findByStudentIdOrderByEarnedAtDesc(Long studentId);

    boolean existsByStudentIdAndAchievementId(Long studentId, Long achievementId);
}
