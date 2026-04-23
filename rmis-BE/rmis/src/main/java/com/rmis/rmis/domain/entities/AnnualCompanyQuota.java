//package com.rmis.rmis.domain.entities;
//
//import jakarta.persistence.*;
//import lombok.AllArgsConstructor;
//import lombok.Getter;
//import lombok.NoArgsConstructor;
//import lombok.Setter;
//
//import java.math.BigDecimal;
//
//@Entity
//@Table(name = "annual_company_quota")
//@Getter
//@Setter
//@NoArgsConstructor
//@AllArgsConstructor
//public class AnnualCompanyQuota {
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(
//            name = "company_id",
//            nullable = false,
//            foreignKey = @ForeignKey(name = "fk_annual_quota_company")
//    )
//    private Company company;
//
//    private BigDecimal annualQuota;
//}
