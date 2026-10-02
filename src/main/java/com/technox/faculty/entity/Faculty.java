package com.technox.faculty.entity;

import com.technox.common.entity.BaseEntity;
import com.technox.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "faculty")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Faculty extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", unique = true, nullable = false)
    private User user;

    @Column(name = "faculty_code", unique = true, nullable = false, length = 30)
    private String facultyCode;

    @Column(name = "designation", nullable = false, length = 100)
    private String designation;

    @Column(name = "department", nullable = false, length = 100)
    private String department;

    @Column(name = "specialization", length = 150)
    private String specialization;

    @Column(name = "office_room", length = 50)
    private String officeRoom;

    @Column(name = "avatar_url", length = 255)
    private String avatarUrl;
}
