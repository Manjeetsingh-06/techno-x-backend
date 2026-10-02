-- V5__registrations_waitlist.sql
-- registrations and waitlist_entries tables

CREATE TABLE registrations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    registration_id VARCHAR(50) NOT NULL UNIQUE,
    event_id BIGINT NOT NULL,
    student_id BIGINT NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'REGISTERED',
    registered_at DATETIME NOT NULL,
    digital_pass_id VARCHAR(60) UNIQUE,
    qr_token VARCHAR(120) UNIQUE,
    pass_validity VARCHAR(30) DEFAULT 'VALID',
    is_manual_override BOOLEAN NOT NULL DEFAULT FALSE,
    override_reason VARCHAR(255),
    overridden_by_user_id BIGINT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT uk_event_student UNIQUE (event_id, student_id),
    CONSTRAINT fk_reg_event FOREIGN KEY (event_id) REFERENCES events(id) ON DELETE CASCADE,
    CONSTRAINT fk_reg_student FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE waitlist_entries (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    event_id BIGINT NOT NULL,
    student_id BIGINT NOT NULL,
    position INT NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'WAITLISTED',
    promoted_at DATETIME NULL,
    promoted_by_user_id BIGINT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT uk_waitlist_event_student UNIQUE (event_id, student_id),
    CONSTRAINT fk_wl_event FOREIGN KEY (event_id) REFERENCES events(id) ON DELETE CASCADE,
    CONSTRAINT fk_wl_student FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
