// package com.rmis.rmis.domain.entities;

// import java.time.LocalDateTime;

// import com.rmis.rmis.enums.QuotaRequestStatus;

// import jakarta.persistence.ForeignKey;
// import jakarta.persistence.Column;
// import jakarta.persistence.Entity;
// import jakarta.persistence.EnumType;
// import jakarta.persistence.Enumerated;
// import jakarta.persistence.FetchType;
// import jakarta.persistence.GeneratedValue;
// import jakarta.persistence.GenerationType;
// import jakarta.persistence.Id;
// import jakarta.persistence.JoinColumn;
// import jakarta.persistence.ManyToOne;
// import jakarta.persistence.PrePersist;
// import jakarta.persistence.Table;
// import lombok.AllArgsConstructor;
// import lombok.Getter;
// import lombok.NoArgsConstructor;
// import lombok.Setter;

// @Entity
// @Getter
// @Setter
// @AllArgsConstructor
// @NoArgsConstructor
// @Table(name = "QuotaRequestHeader")
// public class QuotaRequestHeader {

//     @Id
//     @GeneratedValue(strategy = GenerationType.IDENTITY)
//     private Long id;

//     // @Column(nullable = false)
//     // private String companyEmail;

//     @Column(nullable = false)
//     private Long requestQuata;

//     @ManyToOne(fetch = FetchType.LAZY)
//     @JoinColumn(name = "company_id", nullable = false, foreignKey = @ForeignKey(name = "fk_quota_requests_company"))
//     private Company company;

//     @Enumerated(EnumType.STRING)
//     private QuotaRequestStatus status;

//     @Column(nullable = false, updatable = false)
//     private LocalDateTime submissionDate;

//     @PrePersist
//     protected void onCreate(){
//         this.submissionDate = LocalDateTime.now();
//         this.status = QuotaRequestStatus.PENDING;
//     }

// }
