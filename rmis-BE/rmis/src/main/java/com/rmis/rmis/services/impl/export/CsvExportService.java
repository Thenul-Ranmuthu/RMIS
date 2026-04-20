package com.rmis.rmis.services.impl.export;

import com.rmis.rmis.domain.dtos.ExportReportDto;
import com.rmis.rmis.domain.enums.ExportFormat;
import com.rmis.rmis.exceptions.ExportException;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

@Service
public class CsvExportService extends AbstractExportService {

    @Override
    public ExportFormat getSupportedFormat() {
        return ExportFormat.CSV;
    }

    @Override
    public String getContentType() {
        return "text/csv;charset=UTF-8";
    }

    @Override
    protected String getExtension() {
        return "csv";
    }

    @Override
    public byte[] generate(ExportReportDto report) throws ExportException {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             PrintWriter writer = new PrintWriter(
                     new OutputStreamWriter(baos, StandardCharsets.UTF_8))) {

            // Report header
            writer.println("# " + REPORT_TITLE);
            writer.println("# " + MINISTRY_NAME);
            writer.println("# Generated: " + report.getGeneratedAt().format(DISPLAY_DATETIME_FMT));
            writer.println("#");

            // System summary block
            writer.println("# SYSTEM SUMMARY");
            writer.println("# Total Annual Budget," +
                    formatQuota(report.getSystemSummary().getTotalApprovedQuota()) + " ton");
            writer.println("# Total Used," +
                    formatQuota(report.getSystemSummary().getTotalUsedQuota()) + " ton");
            writer.println("# Total Remaining," +
                    formatQuota(report.getSystemSummary().getTotalRemainingQuota()) + " ton");
            writer.println("# Active Companies," +
                    report.getSystemSummary().getTotalCompanies());
            writer.println("#");

            // Column headers
            writer.println(String.join(",", CSV_HEADERS));

            // Data rows
            for (ExportReportDto.CompanyRow row : report.getCompanyRows()) {
                writer.println(buildCsvRow(row));
            }

            writer.flush();
            return baos.toByteArray();

        } catch (IOException e) {
            throw new ExportException("CSV", "Failed to generate CSV export", e);
        }
    }

    // Builds one CSV data row. Wraps string fields in quotes to handle company names that may contain commas.

    private String buildCsvRow(ExportReportDto.CompanyRow row) {
        return String.join(",",
                csvEscape(row.getCompanyName()),
                csvEscape(row.getRegistrationNumber()),
                formatQuota(row.getAllocatedQuota()),
                formatQuota(row.getUsedQuota()),
                formatQuota(row.getRemainingQuota()),
                formatPercentage(row.getUsagePercentage())
        );
    }

    private String csvEscape(String value) {
        if (value == null) return "\"\"";
        return "\"" + value.replace("\"", "\"\"") + "\"";
    }
}
