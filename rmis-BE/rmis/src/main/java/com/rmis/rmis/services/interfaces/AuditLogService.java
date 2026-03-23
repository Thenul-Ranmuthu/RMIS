package com.rmis.rmis.services.interfaces;

import com.rmis.rmis.domain.dtos.AuditLogResponseDto;
import com.rmis.rmis.domain.entities.MinistryOfficer;
import com.rmis.rmis.domain.entities.QuotaRequest;

import java.time.LocalDate;
import java.util.List;

public interface AuditLogService {

    void logApproval(MinistryOfficer officer, QuotaRequest request);

    void logRejection(MinistryOfficer officer, QuotaRequest request);

    List<AuditLogResponseDto> getAuditLogs(LocalDate from, LocalDate to);
}
