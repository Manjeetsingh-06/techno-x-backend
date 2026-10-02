-- V4__events.sql
-- event_categories, clubs, and events tables

CREATE TABLE event_categories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255),
    color_code VARCHAR(20),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE clubs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    tagline VARCHAR(150),
    description TEXT,
    logo_url VARCHAR(255),
    faculty_coordinator VARCHAR(100),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE events (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    uuid VARCHAR(36) NOT NULL UNIQUE,
    title VARCHAR(150) NOT NULL,
    slug VARCHAR(150),
    description TEXT NOT NULL,
    category_id BIGINT NOT NULL,
    club_id BIGINT NULL,
    committee_code VARCHAR(50),
    organizer VARCHAR(100) NOT NULL,
    banner_url VARCHAR(500),
    event_date DATE NOT NULL,
    start_time TIME,
    end_time TIME,
    venue VARCHAR(150) NOT NULL,
    capacity INT NOT NULL,
    registered_count INT NOT NULL DEFAULT 0,
    waitlist_count INT NOT NULL DEFAULT 0,
    registration_deadline DATETIME NOT NULL,
    eligibility VARCHAR(255),
    rules TEXT,
    requirements TEXT,
    status VARCHAR(30) NOT NULL DEFAULT 'DRAFT',
    created_by_user_id BIGINT NOT NULL,
    faculty_coordinator VARCHAR(100),
    committee_coordinator VARCHAR(100),
    approved_by_user_id BIGINT NULL,
    approved_at DATETIME NULL,
    approval_reason VARCHAR(255),
    version BIGINT DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_event_category FOREIGN KEY (category_id) REFERENCES event_categories(id),
    CONSTRAINT fk_event_club FOREIGN KEY (club_id) REFERENCES clubs(id) ON DELETE SET NULL,
    CONSTRAINT fk_event_creator FOREIGN KEY (created_by_user_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
