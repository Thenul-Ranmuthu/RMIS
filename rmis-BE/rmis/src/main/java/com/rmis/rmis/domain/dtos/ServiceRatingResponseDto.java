package com.rmis.rmis.domain.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ServiceRatingResponseDto {
    private Long id;
    private Long serviceTicketId;
    private Integer rating;
    private String feedback;
    private LocalDateTime createdAt;
    private String reviewerName;
}
