package com.rmis.rmis.services.impl.export;

import com.rmis.rmis.domain.dtos.AnalyticsDashboardDto;
import com.rmis.rmis.domain.dtos.CompanyQuotaBreakdownDto;
import com.rmis.rmis.domain.dtos.ExportReportDto;
import com.rmis.rmis.domain.enums.ExportFormat;
import com.rmis.rmis.exceptions.ExportException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QuotaExportOrchestrator {

    private final com.rmis.rmis.analytics.strategy.QuotaAggregationStrategy aggregationStrategy;
    private final ExportServiceFactory exportServiceFactory;

    /**
     * Entry point called by the controller.
     * 1. Runs the analytics aggregation (same data source as the dashboard)
     * 2. Flattens it into ExportReportDTO
     * 3. Delegates to the correct ExportService
     */
    @Transactional(readOnly = true)
    public ExportResult export(ExportFormat format) throws ExportException {
        // Reuse existing aggregation — no new queries
        AnalyticsDashboardDto dashboard = aggregationStrategy.aggregate();

        ExportReportDto report = buildReport(dashboard);
        ExportService   service = exportServiceFactory.getService(format);
        byte[]          bytes   = service.generate(report);

        return new ExportResult(
                bytes,
                service.getContentType(),
                service.getFilename(report)
        );
    }

    // DTO assembly

    private ExportReportDto buildReport(AnalyticsDashboardDto dashboard) {
        List<ExportReportDto.CompanyRow> rows = dashboard.getCompanyBreakdowns()
                .stream()
                .map(this::toCompanyRow)
                .collect(Collectors.toList());

        return ExportReportDto.builder()
                .generatedAt(dashboard.getGeneratedAt())
                .systemSummary(dashboard.getSystemSummary())
                .companyRows(rows)
                .build();
    }

    private ExportReportDto.CompanyRow toCompanyRow(CompanyQuotaBreakdownDto dto) {
        return ExportReportDto.CompanyRow.builder()
                .companyName(dto.getCompanyName())
                .registrationNumber(dto.getRegistrationNumber())
                .allocatedQuota(dto.getApprovedQuota())
                .usedQuota(dto.getUsedQuota())
                .remainingQuota(dto.getRemainingQuota())
                .usagePercentage(dto.getUsagePercentage())
                .build();
    }

    // Result value object

    // Immutable value object carrying everything the controller needs to write the HTTP response — keeps the controller thin.
    public record ExportResult(
            byte[] bytes,
            String contentType,
            String filename
    ) {}
}
