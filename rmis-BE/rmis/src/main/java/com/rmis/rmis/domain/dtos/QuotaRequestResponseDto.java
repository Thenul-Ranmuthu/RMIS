package com.rmis.rmis.domain.dtos;

import com.rmis.rmis.enums.QuotaRequestStatus;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuotaRequestResponseDto {

    @JsonProperty("request_id")
    private String requestId;

    @JsonProperty("company_name")
    private String companyName;

    @JsonProperty("requested_quota")
    private BigDecimal requestedQuota;

    @JsonProperty("submission_date")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime submissionDate;

    @JsonProperty("status")
    private QuotaRequestStatus status;
}