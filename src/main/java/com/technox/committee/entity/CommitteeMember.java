package com.technox.committee.entity;

import com.technox.common.entity.BaseEntity;
import com.technox.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "committee_members")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommitteeMember extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", unique = true, nullable = false)
    private User user;

    @Column(name = "committee_code", nullable = false, length = 30)
    private String committeeCode; // e.g. ABHIVYAKTI, KIRAN, OORJA, DARPAN, SANJEEVANI, SRIJAN

    @Column(name = "committee_name", nullable = false, length = 100)
    private String committeeName;

    @Column(name = "role_title", nullable = false, length = 100)
    private String roleTitle; // e.g. "Convener", "Lead Coordinator"

    @Column(name = "department", length = 100)
    private String department;
}
