package com.rmis.rmis.domain.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "annual_quota_distribution")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AnnualQuotaDistribution {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "annual_quota")
    private BigDecimal annualQuota;  // set by admin

}
