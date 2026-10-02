-- V2__roles_permissions.sql
-- Seed Roles and Permissions and associate them

INSERT INTO roles (id, name, description, created_at, updated_at) VALUES
(1, 'ADMIN', 'System Administrator with full access', NOW(), NOW()),
(2, 'FACULTY', 'Faculty Member / Head with event oversight and approval rights', NOW(), NOW()),
(3, 'MANAGEMENT_COMMITTEE', 'College Committee Member responsible for organizing events', NOW(), NOW()),
(4, 'STUDENT', 'Enrolled college student who registers and participates in events', NOW(), NOW());

INSERT INTO permissions (name, description, created_at, updated_at) VALUES
('USER_VIEW', 'View system users', NOW(), NOW()),
('USER_MANAGE', 'Create, update, manage system users', NOW(), NOW()),
('STUDENT_VIEW', 'View student profiles and records', NOW(), NOW()),
('STUDENT_MANAGE', 'Manage student profiles and statuses', NOW(), NOW()),
('FACULTY_VIEW', 'View faculty profiles and departments', NOW(), NOW()),
('FACULTY_MANAGE', 'Manage faculty profiles and roles', NOW(), NOW()),
('COMMITTEE_VIEW', 'View committee members and hierarchy', NOW(), NOW()),
('COMMITTEE_MANAGE', 'Manage committee members and assignments', NOW(), NOW()),
('EVENT_CREATE', 'Create new events', NOW(), NOW()),
('EVENT_VIEW', 'View events listing and details', NOW(), NOW()),
('EVENT_EDIT', 'Edit existing event details', NOW(), NOW()),
('EVENT_APPROVE', 'Approve or reject submitted events', NOW(), NOW()),
('EVENT_PUBLISH', 'Publish approved events to public', NOW(), NOW()),
('EVENT_CANCEL', 'Cancel published or planned events', NOW(), NOW()),
('EVENT_MANAGE', 'Full management of all events', NOW(), NOW()),
('REGISTRATION_VIEW', 'View event registrations', NOW(), NOW()),
('REGISTRATION_CREATE', 'Register for events', NOW(), NOW()),
('REGISTRATION_MANAGE', 'Manage registration statuses', NOW(), NOW()),
('REGISTRATION_OVERRIDE', 'Manually override capacity and registrations', NOW(), NOW()),
('WAITLIST_VIEW', 'View event waitlists', NOW(), NOW()),
('WAITLIST_APPROVE', 'Promote waitlisted students to registered', NOW(), NOW()),
('ATTENDANCE_VIEW', 'View event attendance rosters', NOW(), NOW()),
('ATTENDANCE_MARK', 'Mark attendance via QR or manual scan', NOW(), NOW()),
('ATTENDANCE_CORRECT', 'Correct existing attendance records', NOW(), NOW()),
('NOTIFICATION_SEND', 'Send notifications and broadcast announcements', NOW(), NOW()),
('NOTIFICATION_VIEW', 'View received notifications', NOW(), NOW()),
('ANALYTICS_VIEW', 'Access system and event analytics dashboards', NOW(), NOW()),
('REPORT_VIEW', 'View generated reports', NOW(), NOW()),
('REPORT_EXPORT', 'Export event and attendance reports as CSV/PDF', NOW(), NOW()),
('CATEGORY_MANAGE', 'Manage event categories', NOW(), NOW()),
('CLUB_MANAGE', 'Manage college clubs', NOW(), NOW()),
('ACHIEVEMENT_MANAGE', 'Manage student badges and achievements', NOW(), NOW()),
('ACHIEVEMENT_VIEW', 'View student badges and achievements', NOW(), NOW()),
('AUDIT_VIEW', 'Inspect system audit trails', NOW(), NOW()),
('SYSTEM_MANAGE', 'Configure platform settings', NOW(), NOW());

-- Map ADMIN to ALL permissions
INSERT INTO role_permissions (role_id, permission_id)
SELECT 1, id FROM permissions;

-- Map FACULTY permissions
INSERT INTO role_permissions (role_id, permission_id)
SELECT 2, id FROM permissions WHERE name IN (
    'EVENT_CREATE', 'EVENT_VIEW', 'EVENT_EDIT', 'EVENT_APPROVE', 'EVENT_CANCEL',
    'REGISTRATION_VIEW', 'ATTENDANCE_VIEW', 'ATTENDANCE_MARK', 'ATTENDANCE_CORRECT',
    'NOTIFICATION_SEND', 'NOTIFICATION_VIEW', 'ANALYTICS_VIEW', 'REPORT_VIEW', 'REPORT_EXPORT',
    'STUDENT_VIEW', 'FACULTY_VIEW', 'ACHIEVEMENT_VIEW'
);

-- Map MANAGEMENT_COMMITTEE permissions
INSERT INTO role_permissions (role_id, permission_id)
SELECT 3, id FROM permissions WHERE name IN (
    'EVENT_CREATE', 'EVENT_VIEW', 'EVENT_EDIT', 'EVENT_PUBLISH',
    'REGISTRATION_VIEW', 'REGISTRATION_MANAGE', 'REGISTRATION_OVERRIDE',
    'WAITLIST_VIEW', 'WAITLIST_APPROVE', 'ATTENDANCE_VIEW', 'ATTENDANCE_MARK',
    'NOTIFICATION_SEND', 'NOTIFICATION_VIEW', 'ANALYTICS_VIEW', 'REPORT_VIEW',
    'STUDENT_VIEW', 'COMMITTEE_VIEW', 'ACHIEVEMENT_VIEW'
);

-- Map STUDENT permissions
INSERT INTO role_permissions (role_id, permission_id)
SELECT 4, id FROM permissions WHERE name IN (
    'EVENT_VIEW', 'REGISTRATION_CREATE', 'REGISTRATION_VIEW',
    'NOTIFICATION_VIEW', 'ACHIEVEMENT_VIEW'
);
