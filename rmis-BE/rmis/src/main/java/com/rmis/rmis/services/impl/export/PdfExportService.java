package com.rmis.rmis.services.impl.export;

import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.draw.SolidLine;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.rmis.rmis.domain.dtos.ExportReportDto;
import com.rmis.rmis.domain.enums.ExportFormat;
import com.rmis.rmis.exceptions.ExportException;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Service
public class PdfExportService extends AbstractExportService {

    // Brand colours
    private static final DeviceRgb LIGHT_TABLE_BG = new DeviceRgb(250, 247, 242); // warm off-white
    private static final DeviceRgb LIGHT_ROW_ALT  = new DeviceRgb(242, 238, 232); // slightly darker beige
    private static final DeviceRgb LIGHT_BORDER   = new DeviceRgb(220, 215, 205); // subtle border
    private static final DeviceRgb DARK_BG      = new DeviceRgb(10,  13,  19);
    private static final DeviceRgb GOLD         = new DeviceRgb(200, 169, 110);
    private static final DeviceRgb RED_QUOTA    = new DeviceRgb(201, 110, 110);
    private static final DeviceRgb GREEN_QUOTA  = new DeviceRgb(61,  139, 110);
    private static final DeviceRgb TEAL         = new DeviceRgb(74,  124, 142);
    private static final DeviceRgb LIGHT_TEXT   = new DeviceRgb(220, 220, 220);
    private static final DeviceRgb DIM_TEXT     = new DeviceRgb(140, 140, 140);
    private static final DeviceRgb WHITE      = new DeviceRgb(255,  255,  255);
    private static final DeviceRgb HEADER_BG    = new DeviceRgb(18,  22,  30);

    @Override
    public ExportFormat getSupportedFormat() { return ExportFormat.PDF; }

    @Override
    public String getContentType() { return "application/pdf"; }

    @Override
    protected String getExtension() { return "pdf"; }

    @Override
    public byte[] generate(ExportReportDto report) throws ExportException {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            PdfWriter   writer  = new PdfWriter(baos);
            PdfDocument pdfDoc  = new PdfDocument(writer);
            Document    doc     = new Document(pdfDoc, PageSize.A4.rotate()); // landscape for table

            pdfDoc.addNewPage();

            PdfFont bold    = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
            PdfFont regular = PdfFontFactory.createFont(StandardFonts.HELVETICA);
            PdfFont mono    = PdfFontFactory.createFont(StandardFonts.COURIER);

            // Set dark background on all pages via page event
            doc.setBackgroundColor(DARK_BG);

            // Cover / header block
            addHeader(doc, bold, regular, report);

            // System summary card
            addSystemSummary(doc, bold, regular, mono, report);

            // Data table
            addCompanyTable(doc, bold, regular, mono, report);

            // Footer
            addFooter(doc, regular, report);

            doc.close();
            return baos.toByteArray();

        } catch (IOException e) {
            throw new ExportException("PDF", "Failed to generate PDF export", e);
        }
    }

    // Section builders

    private void addHeader(Document doc, PdfFont bold, PdfFont regular,
                           ExportReportDto report) throws IOException {

        // Ministry name (small caps style)
        doc.add(new Paragraph(MINISTRY_NAME.toUpperCase())
                .setFont(bold).setFontSize(7f)
                .setFontColor(GOLD)
                .setCharacterSpacing(2f)
                .setMarginBottom(4f));

        // Report title
        doc.add(new Paragraph(REPORT_TITLE)
                .setFont(bold).setFontSize(20f)
                .setFontColor(LIGHT_TEXT)
                .setMarginBottom(2f));

        // Thin gold rule
        SolidLine line = new SolidLine(1.5f);
        line.setColor(GOLD);

        doc.add(new LineSeparator(line));

        // Generated timestamp
        doc.add(new Paragraph(
                "Generated: " + report.getGeneratedAt().format(DISPLAY_DATETIME_FMT))
                .setFont(regular).setFontSize(8f)
                .setFontColor(DIM_TEXT)
                .setMarginTop(4f)
                .setMarginBottom(16f));
    }

    private void addSystemSummary(Document doc, PdfFont bold, PdfFont regular,
                                  PdfFont mono, ExportReportDto report) {

        doc.add(new Paragraph("SYSTEM SUMMARY")
                .setFont(bold).setFontSize(7f)
                .setFontColor(TEAL)
                .setCharacterSpacing(2f)
                .setMarginBottom(6f));

        // 3-column summary table
        Table summary = new Table(UnitValue.createPercentArray(new float[]{1, 1, 1}))
                .useAllAvailableWidth()
                .setMarginBottom(20f);

        addSummaryCell(summary, bold, mono,
                "Annual Budget",
                formatQuota(report.getSystemSummary().getTotalApprovedQuota()) + " ton",
                GOLD);
        addSummaryCell(summary, bold, mono,
                "Total Used",
                formatQuota(report.getSystemSummary().getTotalUsedQuota()) + " ton",
                RED_QUOTA);
        addSummaryCell(summary, bold, mono,
                "Total Remaining",
                formatQuota(report.getSystemSummary().getTotalRemainingQuota()) + " ton",
                GREEN_QUOTA);

        doc.add(summary);
    }

    private void addSummaryCell(Table table, PdfFont bold, PdfFont mono,
                                String label, String value, DeviceRgb accent) {
        Cell cell = new Cell()
                .setBackgroundColor(HEADER_BG)
                .setBorder(Border.NO_BORDER)
                .setBorderLeft(new SolidBorder(accent, 3))
                .setPadding(10f)
                .setMargin(4f);

        cell.add(new Paragraph(label.toUpperCase())
                .setFont(bold).setFontSize(6.5f)
                .setFontColor(DIM_TEXT)
                .setCharacterSpacing(1.5f)
                .setMarginBottom(4f));

        cell.add(new Paragraph(value)
                .setFont(bold).setFontSize(14f)
                .setFontColor(accent));

        table.addCell(cell);
    }

    private void addCompanyTable(Document doc, PdfFont bold, PdfFont regular,
                                 PdfFont mono, ExportReportDto report) {

        doc.add(new Paragraph("PER-COMPANY BREAKDOWN")
                .setFont(bold).setFontSize(7f)
                .setFontColor(TEAL)
                .setCharacterSpacing(2f)
                .setMarginBottom(6f));

        // 6 columns: name, reg, allocated, used, remaining, usage%
        float[] colWidths = {3, 1.5f, 1.5f, 1.5f, 1.5f, 1.2f};
        Table table = new Table(UnitValue.createPercentArray(colWidths))
                .useAllAvailableWidth()
                .setMarginBottom(16f);

        // Header row
        for (String header : CSV_HEADERS) {
            table.addHeaderCell(
                    new Cell()
                            .setBackgroundColor(HEADER_BG)
                            .setBorderBottom(new SolidBorder(GOLD, 1))
                            .setBorderTop(Border.NO_BORDER)
                            .setBorderLeft(Border.NO_BORDER)
                            .setBorderRight(Border.NO_BORDER)
                            .setPadding(6f)
                            .add(new Paragraph(header.toUpperCase())
                                    .setFont(bold).setFontSize(6f)
                                    .setFontColor(WHITE)
                                    .setCharacterSpacing(1f))
            );
        }

        // Data rows
        boolean alt = false;
        for (ExportReportDto.CompanyRow row : report.getCompanyRows()) {
            DeviceRgb rowBg = alt ? LIGHT_ROW_ALT : LIGHT_TABLE_BG;
            alt = !alt;

            double pct = row.getUsagePercentage();
            DeviceRgb usageColor = pct > 85 ? RED_QUOTA : pct > 60 ? GOLD : GREEN_QUOTA;

            addDataCell(table, row.getCompanyName(), rowBg, bold,    new DeviceRgb(40,40,40), 8f);
            addDataCell(table, row.getRegistrationNumber(), rowBg, mono, new DeviceRgb(40,40,40), 7.5f);
            addDataCell(table, formatQuota(row.getAllocatedQuota()) + " ton", rowBg, mono, new DeviceRgb(40,40,40), 7.5f);
            addDataCell(table, formatQuota(row.getUsedQuota()) + " ton",      rowBg, mono, new DeviceRgb(40,40,40), 7.5f);
            addDataCell(table, formatQuota(row.getRemainingQuota()) + " ton", rowBg, mono, new DeviceRgb(40,40,40),  7.5f);
            addDataCell(table, formatPercentage(pct) + "%",                  rowBg, bold, usageColor, 8f);
        }

        doc.add(table);
    }

    private void addDataCell(Table table, String text, DeviceRgb bg,
                             PdfFont font, DeviceRgb color, float size) {
        table.addCell(
                new Cell()
                        .setBackgroundColor(bg)
                        .setBorder(Border.NO_BORDER)
                        .setBorderBottom(new SolidBorder(LIGHT_BORDER, 0.5f))
                        .setPadding(6f)
                        .add(new Paragraph(text != null ? text : "")
                                .setFont(font).setFontSize(size)
                                .setFontColor(color))
        );
    }

    private void addFooter(Document doc, PdfFont regular, ExportReportDto report) {
        doc.add(new Paragraph(
                "This report is auto-generated by the RMIS platform. " +
                        "Active companies: " + report.getSystemSummary().getTotalCompanies() +
                        " · Approved requests: " + report.getSystemSummary().getTotalApprovedRequests())
                .setFont(regular).setFontSize(7f)
                .setFontColor(DIM_TEXT)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginTop(8f));
    }
}
