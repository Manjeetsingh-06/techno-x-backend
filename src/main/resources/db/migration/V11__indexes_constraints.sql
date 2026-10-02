-- V11__indexes_constraints.sql
-- Performance and search indexes

CREATE INDEX idx_users_status ON users(status);
CREATE INDEX idx_users_email ON users(email);

CREATE INDEX idx_students_course_dept ON students(course, department);
CREATE INDEX idx_students_qr ON students(qr_code);

CREATE INDEX idx_faculty_dept ON faculty(department);

CREATE INDEX idx_events_status_date ON events(status, event_date);
CREATE INDEX idx_events_category ON events(category_id);
CREATE INDEX idx_events_club ON events(club_id);
CREATE INDEX idx_events_creator ON events(created_by_user_id);
CREATE INDEX idx_events_committee ON events(committee_code);

CREATE INDEX idx_reg_event_status ON registrations(event_id, status);
CREATE INDEX idx_reg_student ON registrations(student_id);
CREATE INDEX idx_reg_qr ON registrations(qr_token);
CREATE INDEX idx_reg_pass ON registrations(digital_pass_id);

CREATE INDEX idx_wl_event_pos ON waitlist_entries(event_id, position);
CREATE INDEX idx_wl_status ON waitlist_entries(status);

CREATE INDEX idx_att_event_status ON attendance(event_id, status);
CREATE INDEX idx_att_scanned_at ON attendance(scanned_at);

CREATE INDEX idx_notif_recipient_read ON notifications(recipient_user_id, is_read);

CREATE INDEX idx_audit_actor ON audit_logs(actor_user_id);
CREATE INDEX idx_audit_action ON audit_logs(action);
CREATE INDEX idx_audit_target ON audit_logs(target_type, target_id);
CREATE INDEX idx_audit_created ON audit_logs(created_at);
