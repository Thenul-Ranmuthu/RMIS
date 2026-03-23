package com.rmis.rmis.repositories;

import com.rmis.rmis.domain.entities.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    // Fetches all audit logs within a date range — newest first
    // SQL: SELECT * FROM audit_logs WHERE timestamp BETWEEN ? AND ? ORDER BY timestamp DESC
    List<AuditLog> findByTimestampBetweenOrderByTimestampDesc(
            LocalDateTime from,
            LocalDateTime to
    );

    // Fetches all audit logs for a specific officer — useful for future officer-level audit
    List<AuditLog> findByOfficerEmailOrderByTimestampDesc(String officerEmail);
}
