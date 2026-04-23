package com.rmis.rmis.services.impl.export;

import com.rmis.rmis.domain.dtos.ExportReportDto;
import com.rmis.rmis.domain.enums.ExportFormat;
import com.rmis.rmis.exceptions.ExportException;

public interface ExportService {
    ExportFormat getSupportedFormat();
    byte[] generate(ExportReportDto report) throws ExportException;
    String getContentType();
    String getFilename(ExportReportDto report);
}