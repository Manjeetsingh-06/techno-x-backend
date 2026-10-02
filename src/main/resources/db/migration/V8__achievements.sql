-- V8__achievements.sql
-- achievements and student_achievements tables

CREATE TABLE achievements (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    title VARCHAR(100) NOT NULL,
    description VARCHAR(255),
    badge_icon VARCHAR(50),
    color_theme VARCHAR(50),
    criteria VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE student_achievements (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    student_id BIGINT NOT NULL,
    achievement_id BIGINT NOT NULL,
    earned_at DATETIME NOT NULL,
    associated_event_title VARCHAR(150),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT uk_student_achievement UNIQUE (student_id, achievement_id),
    CONSTRAINT fk_sa_student FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE,
    CONSTRAINT fk_sa_achievement FOREIGN KEY (achievement_id) REFERENCES achievements(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Seed default badges/achievements
INSERT INTO achievements (code, title, description, badge_icon, color_theme, criteria, created_at, updated_at) VALUES
('FIRST_STEP', 'First Step', 'Registered for your first Techno event', 'Trophy', 'emerald', 'Register for 1 event', NOW(), NOW()),
('TECH_ENTHUSIAST', 'Tech Enthusiast', 'Attended 3 or more technical events', 'Zap', 'blue', 'Attend 3 technical events', NOW(), NOW()),
('HACKATHON_HERO', 'Hackathon Hero', 'Participated in a 24h or 36h Hackathon', 'Code', 'amber', 'Participate in Hackathon', NOW(), NOW()),
('PERFECT_ATTENDANCE', 'Punctual Prodigy', '100% verified attendance in 5 registered events', 'CheckCircle', 'purple', 'Attend 5 events without absent', NOW(), NOW()),
('CAMPUS_LEADER', 'Campus Pioneer', 'Actively organized or coordinated college events', 'Award', 'rose', 'Coordinate 2 events', NOW(), NOW());
