package com.rmis.rmis.services;

import com.rmis.rmis.domain.entities.QuotaRequest;
import com.rmis.rmis.enums.QuotaRequestStatus;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import jakarta.persistence.criteria.Predicate;

public class QuotaRequestsSpecification {
    public static Specification<QuotaRequest> withFilters(QuotaRequestStatus status,
                                                          String companyName, LocalDate submissionDate) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            //Filter by status
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }

            // Filter by company name
            if(companyName != null && !companyName.isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("companyName")), "%" + companyName.toLowerCase() + "%"));
            }
            // Filter by submission date
            if (submissionDate != null) {
                predicates.add(cb.between(
                        root.get("submissionDate"),
                        submissionDate.atStartOfDay(),              // 2026-03-01 00:00:00
                        submissionDate.plusDays(1).atStartOfDay()   // 2026-03-02 00:00:00
                ));
            }

            // Combine all active filters with AND
            return cb.and(predicates.toArray(new Predicate[0]));


        };
    }
}
