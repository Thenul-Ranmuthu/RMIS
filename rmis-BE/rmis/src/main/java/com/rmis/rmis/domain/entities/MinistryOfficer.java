package com.rmis.rmis.domain.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "ministry_officers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MinistryOfficer {

    // Primary Key
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Identity Fields
    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false, unique = true)
    private String officerId;          // equivalent of companyid in Company entity

    @Column(nullable = false)
    private String password;

    // Role
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "role_id", nullable = false, foreignKey = @ForeignKey(name = "fk_user_role"))
    private Role role;
}
