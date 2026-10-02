-- V6__attendance.sql
-- attendance and attendance_corrections tables

CREATE TABLE attendance (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    event_id BIGINT NOT NULL,
    student_id BIGINT NOT NULL,
    registration_id BIGINT NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'PRESENT',
    scanned_at DATETIME NOT NULL,
    scanned_by_user_id BIGINT NULL,
    operator_name VARCHAR(100),
    operator_role VARCHAR(50),
    is_corrected BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT uk_attendance_event_student UNIQUE (event_id, student_id),
    CONSTRAINT fk_att_event FOREIGN KEY (event_id) REFERENCES events(id) ON DELETE CASCADE,
    CONSTRAINT fk_att_student FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE,
    CONSTRAINT fk_att_registration FOREIGN KEY (registration_id) REFERENCES registrations(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE attendance_corrections (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    attendance_id BIGINT NOT NULL,
    previous_status VARCHAR(30) NOT NULL,
    new_status VARCHAR(30) NOT NULL,
    reason VARCHAR(255) NOT NULL,
    corrected_by_user_id BIGINT NOT NULL,
    corrected_by_name VARCHAR(100),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_att_correction FOREIGN KEY (attendance_id) REFERENCES attendance(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
