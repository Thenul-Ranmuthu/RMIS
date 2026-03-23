package com.rmis.rmis.services.impl;

import com.rmis.rmis.domain.dtos.AuditLogResponseDto;
import com.rmis.rmis.repositories.AuditLogRepository;
import com.rmis.rmis.services.interfaces.AuditLogService;

import com.rmis.rmis.domain.entities.AuditLog;
import com.rmis.rmis.domain.entities.MinistryOfficer;
import com.rmis.rmis.domain.entities.QuotaRequest;
import com.rmis.rmis.domain.enums.AuditActionType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuditLogServiceImpl implements AuditLogService {

    private final AuditLogRepository auditLogRepository;

    @Override
    public void logApproval(MinistryOfficer officer, QuotaRequest request) {
        AuditLog log = AuditLog.builder()
                .officerName(officer.getName())
                .officerEmail(officer.getEmail())
                .actionType(AuditActionType.APPROVED)
                .requestId(formatRequestId(request.getRequestNumber()))
                .rejectionReason(null)
                .build();

        auditLogRepository.save(log);
    }

    @Override
    public void logRejection(MinistryOfficer officer, QuotaRequest request) {
        AuditLog log = AuditLog.builder()
                .officerName(officer.getName())
                .officerEmail(officer.getEmail())
                .actionType(AuditActionType.REJECTED)
                .requestId(formatRequestId(request.getRequestNumber()))
                .build();

        auditLogRepository.save(log);
    }

    @Override
    public List<AuditLogResponseDto> getAuditLogs(LocalDate from, LocalDate to) {
        LocalDateTime fromDateTime = from.atStartOfDay();
        LocalDateTime toDateTime = to.atTime(23, 59, 59);

        return auditLogRepository
                .findByTimestampBetweenOrderByTimestampDesc(fromDateTime, toDateTime)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    private AuditLogResponseDto toDto(AuditLog log) {
        return AuditLogResponseDto.builder()
                .id(log.getId())
                .officerName(log.getOfficerName())
                .officerEmail(log.getOfficerEmail())
                .actionType(log.getActionType())
                .requestId(log.getRequestId())
                .rejectionReason(log.getRejectionReason())
                .timestamp(log.getTimestamp())
                .build();
    }

    private String formatRequestId(Long requestNumber) {
        return String.format("REQ-%04d", requestNumber);
    }
}