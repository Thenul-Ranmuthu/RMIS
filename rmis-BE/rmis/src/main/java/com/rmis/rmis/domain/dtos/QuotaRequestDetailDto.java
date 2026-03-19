package com.rmis.rmis.domain.dtos;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.rmis.rmis.domain.enums.QuotaRequestStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuotaRequestDetailDto {
    @JsonProperty("id")
    private UUID id;

    @JsonProperty("request_id")
    private String requestId;

    // Company Information
    @JsonProperty("company_name")
    private String companyName;

    @JsonProperty("company_email")
    private String companyEmail;

    @JsonProperty("company_id")
    private String companyIdentifier;

    // Request Details
    @JsonProperty("requested_quota")
    private BigDecimal requestedQuota;

    @JsonProperty("submission_date")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime submissionDate;

    @JsonProperty("status")
    private QuotaRequestStatus status;

    // Review Details
    @JsonProperty("reviewed_by")
    private String reviewedBy;

    @JsonProperty("reviewed_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime reviewedAt;
}