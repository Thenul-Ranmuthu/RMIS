package com.rmis.rmis.domain.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "certifications")
public class Certification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;          // e.g. "AWS Certified Developer"

    @Column(nullable = false)
    private String fileName;      // e.g. "a1b2c3d4.pdf" - stored in DB

    @Column(nullable = false)
    private String filePath;      // e.g. "uploads/certifications/1/a1b2c3d4.pdf"

    private String fileType;      // e.g. "application/pdf"

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "technician_id", nullable = false)
    private Technician technician;
}