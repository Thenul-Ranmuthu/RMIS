package com.rmis.rmis.domain.dtos;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.rmis.rmis.domain.enums.AuditActionType;
import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLogResponseDto {

    @JsonProperty("id")
    private Long id;

    @JsonProperty("officer_name")
    private String officerName;

    @JsonProperty("officer_email")
    private String officerEmail;

    @JsonProperty("action_type")
    private AuditActionType actionType;

    @JsonProperty("request_id")
    private String requestId;

    // null for APPROVED actions — only populated for REJECTED
    @JsonProperty("rejection_reason")
    private String rejectionReason;

    @JsonProperty("timestamp")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timestamp;
}
