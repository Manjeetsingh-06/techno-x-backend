package com.technox.achievement.service;

import com.technox.achievement.dto.AchievementDto;
import com.technox.achievement.dto.StudentAchievementDto;
import com.technox.achievement.entity.Achievement;
import com.technox.achievement.entity.StudentAchievement;
import com.technox.achievement.repository.AchievementRepository;
import com.technox.achievement.repository.StudentAchievementRepository;
import com.technox.common.exception.BusinessException;
import com.technox.common.exception.ErrorCode;
import com.technox.student.entity.Student;
import com.technox.student.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AchievementService {

    private final AchievementRepository achievementRepository;
    private final StudentAchievementRepository studentAchievementRepository;
    private final StudentRepository studentRepository;

    @Transactional(readOnly = true)
    public List<AchievementDto> getAllAchievements() {
        return achievementRepository.findAll().stream().map(this::mapToDto).toList();
    }

    @Transactional(readOnly = true)
    public List<StudentAchievementDto> getStudentAchievements(Long studentId) {
        return studentAchievementRepository.findByStudentIdOrderByEarnedAtDesc(studentId)
                .stream().map(this::mapStudentAchievementToDto).toList();
    }

    @Transactional
    public StudentAchievementDto awardAchievement(Long studentId, String achievementCode, String eventTitle) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Student not found: " + studentId));

        Achievement achievement = achievementRepository.findByCode(achievementCode)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Achievement not found: " + achievementCode));

        if (studentAchievementRepository.existsByStudentIdAndAchievementId(studentId, achievement.getId())) {
            log.info("Student {} already holds badge {}", student.getStudentId(), achievementCode);
            return null;
        }

        StudentAchievement record = StudentAchievement.builder()
                .student(student)
                .achievement(achievement)
                .earnedAt(LocalDateTime.now())
                .associatedEventTitle(eventTitle)
                .build();

        record = studentAchievementRepository.save(record);
        log.info("Awarded badge {} to student {}", achievementCode, student.getStudentId());
        return mapStudentAchievementToDto(record);
    }

    public AchievementDto mapToDto(Achievement a) {
        return AchievementDto.builder()
                .id(a.getId())
                .code(a.getCode())
                .title(a.getTitle())
                .description(a.getDescription())
                .badgeIcon(a.getBadgeIcon())
                .colorTheme(a.getColorTheme())
                .criteria(a.getCriteria())
                .build();
    }

    public StudentAchievementDto mapStudentAchievementToDto(StudentAchievement sa) {
        return StudentAchievementDto.builder()
                .id(sa.getId())
                .studentId(sa.getStudent().getId())
                .achievement(mapToDto(sa.getAchievement()))
                .earnedAt(sa.getEarnedAt())
                .associatedEventTitle(sa.getAssociatedEventTitle())
                .build();
    }
}
