package com.rmis.rmis.domain.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PagedResponseDto<T> {

    private List<T> data;

    private long totalRecords;

    private int totalPages;

    private int currentPage;
}