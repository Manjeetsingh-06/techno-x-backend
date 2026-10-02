-- V9__clubs_categories.sql
-- Seed event categories and clubs

INSERT INTO event_categories (name, description, color_code, created_at, updated_at) VALUES
('Technical', 'Coding competitions, hackathons, robotics, and developer conferences', '#3B82F6', NOW(), NOW()),
('Cultural', 'Music festivals, drama, dance, art exhibitions, and cultural showcases', '#EC4899', NOW(), NOW()),
('Workshops', 'Hands-on technical workshops, guest lectures, and skill masterclasses', '#10B981', NOW(), NOW()),
('Sports', 'Inter-department cricket, football, badminton, and athletics tournaments', '#F59E0B', NOW(), NOW()),
('Management', 'Business plan pitching, case study competitions, and leadership summits', '#8B5CF6', NOW(), NOW()),
('Gaming', 'E-sports tournaments, LAN gaming showdowns, and VR simulations', '#EF4444', NOW(), NOW());

INSERT INTO clubs (code, name, tagline, description, faculty_coordinator, is_active, created_at, updated_at) VALUES
('CODING_CLUB', 'Techno Coding Society', 'Code, Build, Innovate', 'Campus flagship club for full-stack engineering, algorithmic problem solving, and open-source contributions.', 'Prof. Amit Verma', TRUE, NOW(), NOW()),
('ROBOTICS_CLUB', 'Robotics & IoT Guild', 'Innovating Automation', 'Students engineering autonomous bots, drone systems, and IoT prototypes for national robotics challenges.', 'Dr. Rajesh Kumar', TRUE, NOW(), NOW()),
('LITERARY_CLUB', 'Faizabad Literary & Debating Society', 'Words That Inspire', 'Fostering public speaking, parliamentary debate, creative writing, and MUN simulations across Lucknow.', 'Dr. Shweta Singh', TRUE, NOW(), NOW()),
('CULTURAL_CLUB', 'Kala & Sanskriti Wing', 'Celebrating Heritage and Art', 'The cultural pulse of Techno Group of Institutions hosting theatricals, music fests, and folk arts.', 'Prof. Neha Gupta', TRUE, NOW(), NOW()),
('SPORTS_GUILD', 'Techno Sports Association', 'Excellence in Action', 'Dedicated to promoting campus athletics, annual sports meets, and inter-college tournament teams.', 'Coach Vikram Rathore', TRUE, NOW(), NOW());
