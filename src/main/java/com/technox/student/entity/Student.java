package com.technox.student.entity;

import com.technox.common.entity.BaseEntity;
import com.technox.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "students")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Student extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", unique = true, nullable = false)
    private User user;

    @Column(name = "student_id", unique = true, nullable = false, length = 30)
    private String studentId;

    @Column(name = "father_name", length = 100)
    private String fatherName;

    @Column(name = "dob", length = 30)
    private String dob;

    @Column(name = "course", nullable = false, length = 50)
    private String course;

    @Column(name = "year", nullable = false, length = 20)
    private String year;

    @Column(name = "semester", length = 20)
    private String semester;

    @Column(name = "department", length = 100)
    private String department;

    @Column(name = "blood_group", length = 10)
    private String bloodGroup;

    @Column(name = "address", length = 255)
    private String address;

    @Column(name = "avatar_url", length = 255)
    private String avatarUrl;

    @Column(name = "qr_code", length = 100)
    private String qrCode;
}
