package com.rmis.rmis.services.impl.export;

import com.rmis.rmis.domain.dtos.ExportReportDto;
import com.rmis.rmis.exceptions.ExportException;

import java.time.format.DateTimeFormatter;

public abstract class AbstractExportService implements ExportService {

    protected static final DateTimeFormatter FILE_DATE_FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd");

    protected static final DateTimeFormatter DISPLAY_DATETIME_FMT =
            DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm");

    protected static final String[] CSV_HEADERS = {
            "Company Name",
            "Registration Number",
            "Allocated Quota (ton)",
            "Used Quota (ton)",
            "Remaining Quota (ton)",
            "Usage (%)"
    };

    protected static final String REPORT_TITLE = "Gas Quota Usage Report";
    protected static final String MINISTRY_NAME = "Environment Ministry of Sri Lanka";

    @Override
    public abstract byte[] generate(ExportReportDto report) throws ExportException;

    // Shared filename pattern: quota-report-YYYY-MM-DD.{ext}
    @Override
    public String getFilename(ExportReportDto report) {
        String date = report.getGeneratedAt().format(FILE_DATE_FMT);
        return "quota-report-" + date + "." + getExtension();
    }

    protected abstract String getExtension();

    protected String formatQuota(java.math.BigDecimal value) {
        return value != null ? value.setScale(2, java.math.RoundingMode.HALF_UP).toPlainString() : "0.00";
    }

    protected String formatPercentage(double value) {
        return String.format("%.1f", value);
    }
}
