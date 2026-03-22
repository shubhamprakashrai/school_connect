package com.schoolmgmt.service;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.HeaderFooter;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Chunk;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.schoolmgmt.model.*;
import com.schoolmgmt.repository.*;
import com.schoolmgmt.util.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class PdfGenerationService {

    private final StudentRepository studentRepository;
    private final ExamResultRepository examResultRepository;
    private final AttendanceRepository attendanceRepository;
    private final TenantSettingsRepository tenantSettingsRepository;
    private final TenantRepository tenantRepository;
    private final SchoolClassRepository schoolClassRepository;
    private final SectionRepository sectionRepository;
    private final FeePaymentRepository feePaymentRepository;

    // Font definitions
    private static final Font TITLE_FONT = new Font(Font.HELVETICA, 18, Font.BOLD, new Color(30, 58, 95));
    private static final Font SUBTITLE_FONT = new Font(Font.HELVETICA, 12, Font.BOLD, new Color(60, 60, 60));
    private static final Font HEADER_FONT = new Font(Font.HELVETICA, 10, Font.BOLD, Color.WHITE);
    private static final Font BODY_FONT = new Font(Font.HELVETICA, 10, Font.NORMAL, Color.BLACK);
    private static final Font BOLD_FONT = new Font(Font.HELVETICA, 10, Font.BOLD, Color.BLACK);
    private static final Font SMALL_FONT = new Font(Font.HELVETICA, 8, Font.NORMAL, new Color(100, 100, 100));
    private static final Font SECTION_FONT = new Font(Font.HELVETICA, 12, Font.BOLD, new Color(30, 58, 95));

    private static final Color PRIMARY_COLOR = new Color(30, 58, 95);
    private static final Color LIGHT_GRAY = new Color(240, 240, 240);
    private static final Color BORDER_COLOR = new Color(200, 200, 200);

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // ===== Report Card PDF =====

    @Transactional(readOnly = true)
    public byte[] generateReportCard(String studentId, UUID academicYearId) {
        String tenantId = TenantContext.requireCurrentTenant();
        UUID studentUUID = UUID.fromString(studentId);

        Student student = studentRepository.findById(studentUUID)
                .orElseThrow(() -> new NoSuchElementException("Student not found: " + studentId));

        TenantSettings settings = tenantSettingsRepository.findByTenantId(tenantId).orElse(null);
        Tenant tenant = tenantRepository.findByIdentifier(tenantId).orElse(null);

        List<ExamResult> results = examResultRepository.findByTenantIdAndStudentId(tenantId, studentUUID);

        // Resolve class and section names
        String className = resolveClassName(student.getCurrentClassId(), tenantId);
        String sectionName = resolveSectionName(student.getCurrentSectionId());

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4, 40, 40, 40, 40);
            PdfWriter writer = PdfWriter.getInstance(document, baos);
            document.open();

            // School header
            addSchoolHeader(document, settings, tenant);

            // Report Card title
            Paragraph title = new Paragraph("REPORT CARD", TITLE_FONT);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(5);
            document.add(title);

            if (settings != null && settings.getAcademicYearStart() != null && settings.getAcademicYearEnd() != null) {
                Paragraph year = new Paragraph(
                        "Academic Year: " + settings.getAcademicYearStart().getYear() + " - " + settings.getAcademicYearEnd().getYear(),
                        SUBTITLE_FONT);
                year.setAlignment(Element.ALIGN_CENTER);
                year.setSpacingAfter(15);
                document.add(year);
            } else {
                document.add(new Paragraph(" ", SMALL_FONT));
            }

            addSeparatorLine(document);

            // Student info section
            addStudentInfoSection(document, student, className, sectionName);

            document.add(new Paragraph(" ", SMALL_FONT));

            // Subject-wise marks table
            addMarksTable(document, results);

            document.add(new Paragraph(" ", SMALL_FONT));

            // Overall summary
            addOverallSummary(document, results);

            document.add(new Paragraph(" ", SMALL_FONT));

            // Attendance summary
            addAttendanceSummary(document, studentUUID, tenantId, settings);

            document.add(new Paragraph(" ", SMALL_FONT));

            // Teacher remarks section
            addRemarksSection(document);

            // Footer
            addFooter(document, writer);

            document.close();
            log.info("Report card PDF generated for student: {}", studentId);
            return baos.toByteArray();

        } catch (Exception e) {
            log.error("Error generating report card PDF for student: {}", studentId, e);
            throw new RuntimeException("Failed to generate report card PDF", e);
        }
    }

    // ===== Fee Receipt PDF =====

    @Transactional(readOnly = true)
    public byte[] generateFeeReceipt(UUID paymentId) {
        String tenantId = TenantContext.requireCurrentTenant();

        FeePayment payment = feePaymentRepository.findById(paymentId)
                .orElseThrow(() -> new NoSuchElementException("Payment not found: " + paymentId));

        if (!payment.getTenantId().equals(tenantId)) {
            throw new IllegalArgumentException("Payment does not belong to current tenant");
        }

        TenantSettings settings = tenantSettingsRepository.findByTenantId(tenantId).orElse(null);
        Tenant tenant = tenantRepository.findByIdentifier(tenantId).orElse(null);

        // Resolve student info
        Student student = studentRepository.findById(payment.getStudentId()).orElse(null);
        String className = student != null ? resolveClassName(student.getCurrentClassId(), tenantId) : "N/A";
        String sectionName = student != null ? resolveSectionName(student.getCurrentSectionId()) : "N/A";

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4, 40, 40, 40, 40);
            PdfWriter writer = PdfWriter.getInstance(document, baos);
            document.open();

            // School header
            addSchoolHeader(document, settings, tenant);

            // Receipt title
            Paragraph title = new Paragraph("FEE RECEIPT", TITLE_FONT);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(15);
            document.add(title);

            addSeparatorLine(document);

            // Receipt info (receipt number, date)
            PdfPTable receiptInfo = new PdfPTable(2);
            receiptInfo.setWidthPercentage(100);
            receiptInfo.setSpacingBefore(10);
            receiptInfo.setSpacingAfter(10);

            addInfoCell(receiptInfo, "Receipt No:", payment.getReceiptNumber() != null ? payment.getReceiptNumber() : "N/A");
            addInfoCell(receiptInfo, "Date:", payment.getPaymentDate() != null ? payment.getPaymentDate().format(DATE_FORMAT) : "N/A");
            addInfoCell(receiptInfo, "Transaction ID:", payment.getTransactionId() != null ? payment.getTransactionId() : "N/A");
            addInfoCell(receiptInfo, "Payment Mode:", payment.getPaymentMode() != null ? payment.getPaymentMode().name() : "N/A");

            document.add(receiptInfo);

            document.add(new Paragraph(" ", SMALL_FONT));

            // Student info
            Paragraph studentHeader = new Paragraph("Student Details", SECTION_FONT);
            studentHeader.setSpacingAfter(8);
            document.add(studentHeader);

            PdfPTable studentInfo = new PdfPTable(2);
            studentInfo.setWidthPercentage(100);
            studentInfo.setSpacingAfter(10);

            addInfoCell(studentInfo, "Student Name:", payment.getStudentName() != null ? payment.getStudentName() : (student != null ? student.getFullName() : "N/A"));
            addInfoCell(studentInfo, "Roll Number:", student != null ? student.getRollNumber() : "N/A");
            addInfoCell(studentInfo, "Class:", className);
            addInfoCell(studentInfo, "Section:", sectionName);

            document.add(studentInfo);

            document.add(new Paragraph(" ", SMALL_FONT));

            // Fee details table
            Paragraph feeHeader = new Paragraph("Fee Details", SECTION_FONT);
            feeHeader.setSpacingAfter(8);
            document.add(feeHeader);

            PdfPTable feeTable = new PdfPTable(2);
            feeTable.setWidthPercentage(100);
            feeTable.setWidths(new float[]{3, 2});

            // Table header
            addTableHeaderCell(feeTable, "Description");
            addTableHeaderCell(feeTable, "Amount");

            // Fee type
            String feeTypeName = payment.getFeeStructure() != null && payment.getFeeStructure().getFeeType() != null
                    ? payment.getFeeStructure().getFeeType().getName()
                    : "Fee Payment";
            String term = payment.getFeeStructure() != null && payment.getFeeStructure().getTerm() != null
                    ? " (" + payment.getFeeStructure().getTerm() + ")"
                    : "";

            addTableCell(feeTable, feeTypeName + term, Element.ALIGN_LEFT);
            addTableCell(feeTable, formatAmount(payment.getTotalAmount(), settings), Element.ALIGN_RIGHT);

            if (payment.getDiscountAmount() != null && payment.getDiscountAmount().compareTo(BigDecimal.ZERO) > 0) {
                addTableCell(feeTable, "Discount", Element.ALIGN_LEFT);
                addTableCell(feeTable, "- " + formatAmount(payment.getDiscountAmount(), settings), Element.ALIGN_RIGHT);
            }

            if (payment.getLateFeeAmount() != null && payment.getLateFeeAmount().compareTo(BigDecimal.ZERO) > 0) {
                addTableCell(feeTable, "Late Fee", Element.ALIGN_LEFT);
                addTableCell(feeTable, "+ " + formatAmount(payment.getLateFeeAmount(), settings), Element.ALIGN_RIGHT);
            }

            document.add(feeTable);

            // Payment summary
            PdfPTable summaryTable = new PdfPTable(2);
            summaryTable.setWidthPercentage(100);
            summaryTable.setWidths(new float[]{3, 2});
            summaryTable.setSpacingBefore(5);

            addSummaryRow(summaryTable, "Amount Paid:", formatAmount(payment.getAmountPaid(), settings));
            addSummaryRow(summaryTable, "Balance Amount:", formatAmount(payment.getBalanceAmount(), settings));
            addSummaryRow(summaryTable, "Payment Status:", payment.getPaymentStatus().name());

            document.add(summaryTable);

            if (payment.getRemarks() != null && !payment.getRemarks().isEmpty()) {
                document.add(new Paragraph(" ", SMALL_FONT));
                Paragraph remarksLabel = new Paragraph("Remarks: " + payment.getRemarks(), BODY_FONT);
                remarksLabel.setSpacingBefore(5);
                document.add(remarksLabel);
            }

            document.add(new Paragraph(" ", SMALL_FONT));
            document.add(new Paragraph(" ", SMALL_FONT));

            // Signature area
            PdfPTable signatureTable = new PdfPTable(2);
            signatureTable.setWidthPercentage(100);
            signatureTable.setSpacingBefore(30);

            PdfPCell collectedCell = new PdfPCell();
            collectedCell.setBorder(Rectangle.NO_BORDER);
            collectedCell.setHorizontalAlignment(Element.ALIGN_LEFT);
            collectedCell.addElement(new Paragraph("_________________________", BODY_FONT));
            collectedCell.addElement(new Paragraph("Collected By: " + (payment.getCollectedBy() != null ? payment.getCollectedBy() : ""), SMALL_FONT));
            signatureTable.addCell(collectedCell);

            PdfPCell stampCell = new PdfPCell();
            stampCell.setBorder(Rectangle.NO_BORDER);
            stampCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            stampCell.addElement(new Paragraph("_________________________", BODY_FONT));
            stampCell.addElement(new Paragraph("Authorized Signatory", SMALL_FONT));
            signatureTable.addCell(stampCell);

            document.add(signatureTable);

            // Disclaimer
            Paragraph disclaimer = new Paragraph("This is a computer-generated receipt and does not require a physical signature.", SMALL_FONT);
            disclaimer.setAlignment(Element.ALIGN_CENTER);
            disclaimer.setSpacingBefore(20);
            document.add(disclaimer);

            document.close();
            log.info("Fee receipt PDF generated for payment: {}", paymentId);
            return baos.toByteArray();

        } catch (Exception e) {
            log.error("Error generating fee receipt PDF for payment: {}", paymentId, e);
            throw new RuntimeException("Failed to generate fee receipt PDF", e);
        }
    }

    // ===== Private Helper Methods =====

    private void addSchoolHeader(Document document, TenantSettings settings, Tenant tenant) throws DocumentException {
        PdfPTable headerTable = new PdfPTable(1);
        headerTable.setWidthPercentage(100);
        headerTable.setSpacingAfter(10);

        PdfPCell headerCell = new PdfPCell();
        headerCell.setBorder(Rectangle.NO_BORDER);
        headerCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        headerCell.setPaddingBottom(10);

        // Logo placeholder
        Paragraph logoPlaceholder = new Paragraph("[School Logo]", SMALL_FONT);
        logoPlaceholder.setAlignment(Element.ALIGN_CENTER);
        headerCell.addElement(logoPlaceholder);

        // School name
        String schoolName = "School Management System";
        if (settings != null && settings.getDisplayName() != null) {
            schoolName = settings.getDisplayName();
        } else if (tenant != null && tenant.getName() != null) {
            schoolName = tenant.getName();
        }

        Font schoolNameFont = new Font(Font.HELVETICA, 20, Font.BOLD, PRIMARY_COLOR);
        Paragraph schoolNamePara = new Paragraph(schoolName, schoolNameFont);
        schoolNamePara.setAlignment(Element.ALIGN_CENTER);
        headerCell.addElement(schoolNamePara);

        // Tagline
        if (settings != null && settings.getTagline() != null) {
            Paragraph tagline = new Paragraph(settings.getTagline(), SMALL_FONT);
            tagline.setAlignment(Element.ALIGN_CENTER);
            headerCell.addElement(tagline);
        }

        // Address
        if (tenant != null && tenant.getAddress() != null) {
            StringBuilder addressStr = new StringBuilder();
            addressStr.append(tenant.getAddress());
            if (tenant.getCity() != null) addressStr.append(", ").append(tenant.getCity());
            if (tenant.getState() != null) addressStr.append(", ").append(tenant.getState());
            if (tenant.getPostalCode() != null) addressStr.append(" - ").append(tenant.getPostalCode());

            Paragraph address = new Paragraph(addressStr.toString(), SMALL_FONT);
            address.setAlignment(Element.ALIGN_CENTER);
            headerCell.addElement(address);
        }

        // Contact
        if (tenant != null) {
            StringBuilder contactStr = new StringBuilder();
            if (tenant.getPhone() != null) contactStr.append("Phone: ").append(tenant.getPhone());
            if (tenant.getEmail() != null) {
                if (!contactStr.isEmpty()) contactStr.append(" | ");
                contactStr.append("Email: ").append(tenant.getEmail());
            }
            if (!contactStr.isEmpty()) {
                Paragraph contact = new Paragraph(contactStr.toString(), SMALL_FONT);
                contact.setAlignment(Element.ALIGN_CENTER);
                headerCell.addElement(contact);
            }
        }

        headerTable.addCell(headerCell);
        document.add(headerTable);
    }

    private void addSeparatorLine(Document document) throws DocumentException {
        PdfPTable separator = new PdfPTable(1);
        separator.setWidthPercentage(100);
        PdfPCell line = new PdfPCell();
        line.setBorderWidthTop(2);
        line.setBorderColorTop(PRIMARY_COLOR);
        line.setBorderWidthBottom(0);
        line.setBorderWidthLeft(0);
        line.setBorderWidthRight(0);
        line.setFixedHeight(2);
        separator.addCell(line);
        separator.setSpacingAfter(10);
        document.add(separator);
    }

    private void addStudentInfoSection(Document document, Student student, String className, String sectionName) throws DocumentException {
        Paragraph header = new Paragraph("Student Information", SECTION_FONT);
        header.setSpacingAfter(8);
        document.add(header);

        PdfPTable infoTable = new PdfPTable(4);
        infoTable.setWidthPercentage(100);
        infoTable.setWidths(new float[]{1.5f, 2.5f, 1.5f, 2.5f});

        addLabelValueCell(infoTable, "Name:", student.getFullName());
        addLabelValueCell(infoTable, "Roll No:", student.getRollNumber());
        addLabelValueCell(infoTable, "Class:", className);
        addLabelValueCell(infoTable, "Section:", sectionName);
        addLabelValueCell(infoTable, "Date of Birth:", student.getDateOfBirth() != null ? student.getDateOfBirth().format(DATE_FORMAT) : "N/A");
        addLabelValueCell(infoTable, "Gender:", student.getGender() != null ? student.getGender().name() : "N/A");
        addLabelValueCell(infoTable, "Father's Name:", student.getFatherName() != null ? student.getFatherName() : "N/A");
        addLabelValueCell(infoTable, "Mother's Name:", student.getMotherName() != null ? student.getMotherName() : "N/A");

        document.add(infoTable);
    }

    private void addMarksTable(Document document, List<ExamResult> results) throws DocumentException {
        Paragraph header = new Paragraph("Subject-wise Performance", SECTION_FONT);
        header.setSpacingAfter(8);
        document.add(header);

        if (results.isEmpty()) {
            document.add(new Paragraph("No exam results available.", BODY_FONT));
            return;
        }

        PdfPTable table = new PdfPTable(6);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{0.5f, 2.5f, 1.5f, 1.5f, 1f, 1.2f});

        // Header row
        addTableHeaderCell(table, "#");
        addTableHeaderCell(table, "Subject");
        addTableHeaderCell(table, "Marks Obtained");
        addTableHeaderCell(table, "Total Marks");
        addTableHeaderCell(table, "Grade");
        addTableHeaderCell(table, "Percentage");

        // Data rows
        int index = 1;
        for (ExamResult result : results) {
            Color rowColor = index % 2 == 0 ? LIGHT_GRAY : Color.WHITE;

            addTableCellWithBackground(table, String.valueOf(index), Element.ALIGN_CENTER, rowColor);
            addTableCellWithBackground(table, result.getExam() != null && result.getExam().getSubjectName() != null
                    ? result.getExam().getSubjectName() : "N/A", Element.ALIGN_LEFT, rowColor);

            if (result.getIsAbsent()) {
                addTableCellWithBackground(table, "ABSENT", Element.ALIGN_CENTER, rowColor);
                addTableCellWithBackground(table, String.valueOf(result.getMaxMarks()), Element.ALIGN_CENTER, rowColor);
                addTableCellWithBackground(table, "-", Element.ALIGN_CENTER, rowColor);
                addTableCellWithBackground(table, "-", Element.ALIGN_CENTER, rowColor);
            } else {
                addTableCellWithBackground(table, String.format("%.1f", result.getMarksObtained()), Element.ALIGN_CENTER, rowColor);
                addTableCellWithBackground(table, String.valueOf(result.getMaxMarks()), Element.ALIGN_CENTER, rowColor);
                addTableCellWithBackground(table, result.getGrade() != null ? result.getGrade() : "-", Element.ALIGN_CENTER, rowColor);
                addTableCellWithBackground(table, result.getPercentage() != null ? String.format("%.1f%%", result.getPercentage()) : "-", Element.ALIGN_CENTER, rowColor);
            }
            index++;
        }

        document.add(table);
    }

    private void addOverallSummary(Document document, List<ExamResult> results) throws DocumentException {
        Paragraph header = new Paragraph("Overall Summary", SECTION_FONT);
        header.setSpacingAfter(8);
        document.add(header);

        double totalMarks = 0;
        double totalMaxMarks = 0;
        int examsTaken = 0;

        for (ExamResult result : results) {
            if (!result.getIsAbsent()) {
                totalMarks += result.getMarksObtained();
                totalMaxMarks += result.getMaxMarks();
                examsTaken++;
            }
        }

        double overallPercentage = totalMaxMarks > 0 ? (totalMarks / totalMaxMarks) * 100 : 0;
        String overallGrade = getGrade(overallPercentage);

        PdfPTable summaryTable = new PdfPTable(4);
        summaryTable.setWidthPercentage(100);
        summaryTable.setWidths(new float[]{1, 1, 1, 1});

        addSummaryCell(summaryTable, "Total Exams", String.valueOf(results.size()));
        addSummaryCell(summaryTable, "Exams Taken", String.valueOf(examsTaken));
        addSummaryCell(summaryTable, "Overall Percentage", String.format("%.1f%%", overallPercentage));
        addSummaryCell(summaryTable, "Overall Grade", overallGrade);

        document.add(summaryTable);

        // Total marks line
        PdfPTable totalTable = new PdfPTable(2);
        totalTable.setWidthPercentage(100);
        totalTable.setSpacingBefore(5);

        PdfPCell totalLabel = new PdfPCell(new Phrase("Total Marks Obtained:", BOLD_FONT));
        totalLabel.setBorder(Rectangle.NO_BORDER);
        totalLabel.setHorizontalAlignment(Element.ALIGN_RIGHT);
        totalLabel.setPaddingRight(10);
        totalTable.addCell(totalLabel);

        PdfPCell totalValue = new PdfPCell(new Phrase(String.format("%.1f / %.1f", totalMarks, totalMaxMarks), BOLD_FONT));
        totalValue.setBorder(Rectangle.NO_BORDER);
        totalValue.setHorizontalAlignment(Element.ALIGN_LEFT);
        totalTable.addCell(totalValue);

        document.add(totalTable);
    }

    private void addAttendanceSummary(Document document, UUID studentId, String tenantId, TenantSettings settings) throws DocumentException {
        Paragraph header = new Paragraph("Attendance Summary", SECTION_FONT);
        header.setSpacingAfter(8);
        document.add(header);

        // Use academic year dates if available, otherwise use current year
        LocalDate startDate;
        LocalDate endDate;
        if (settings != null && settings.getAcademicYearStart() != null && settings.getAcademicYearEnd() != null) {
            startDate = settings.getAcademicYearStart();
            endDate = settings.getAcademicYearEnd();
        } else {
            startDate = LocalDate.now().withMonth(4).withDayOfMonth(1);
            if (LocalDate.now().getMonthValue() < 4) {
                startDate = startDate.minusYears(1);
            }
            endDate = LocalDate.now();
        }

        long totalDays = attendanceRepository.getTotalAttendanceCountForStudent(studentId, startDate, endDate, tenantId);
        long presentDays = attendanceRepository.getPresentAttendanceCountForStudent(studentId, startDate, endDate, tenantId);
        long absentDays = totalDays - presentDays;
        double attendancePercentage = totalDays > 0 ? (presentDays * 100.0 / totalDays) : 0;

        PdfPTable attTable = new PdfPTable(4);
        attTable.setWidthPercentage(100);
        attTable.setWidths(new float[]{1, 1, 1, 1});

        addSummaryCell(attTable, "Total Working Days", String.valueOf(totalDays));
        addSummaryCell(attTable, "Days Present", String.valueOf(presentDays));
        addSummaryCell(attTable, "Days Absent", String.valueOf(absentDays));
        addSummaryCell(attTable, "Attendance %", String.format("%.1f%%", attendancePercentage));

        document.add(attTable);
    }

    private void addRemarksSection(Document document) throws DocumentException {
        Paragraph header = new Paragraph("Teacher's Remarks", SECTION_FONT);
        header.setSpacingAfter(8);
        document.add(header);

        // Empty remarks box
        PdfPTable remarksTable = new PdfPTable(1);
        remarksTable.setWidthPercentage(100);

        PdfPCell remarksCell = new PdfPCell();
        remarksCell.setMinimumHeight(60);
        remarksCell.setBorderColor(BORDER_COLOR);
        remarksCell.setBorderWidth(1);
        remarksCell.setPadding(8);
        remarksCell.addElement(new Paragraph(" ", BODY_FONT));
        remarksTable.addCell(remarksCell);

        document.add(remarksTable);

        // Signature lines
        PdfPTable signTable = new PdfPTable(3);
        signTable.setWidthPercentage(100);
        signTable.setSpacingBefore(30);

        String[] labels = {"Class Teacher", "Principal", "Parent/Guardian"};
        for (String label : labels) {
            PdfPCell cell = new PdfPCell();
            cell.setBorder(Rectangle.NO_BORDER);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.addElement(new Paragraph("_________________________", BODY_FONT));
            Paragraph labelPara = new Paragraph(label, SMALL_FONT);
            labelPara.setAlignment(Element.ALIGN_CENTER);
            cell.addElement(labelPara);
            signTable.addCell(cell);
        }

        document.add(signTable);
    }

    private void addFooter(Document document, PdfWriter writer) throws DocumentException {
        Paragraph footer = new Paragraph(
                "Generated on: " + LocalDate.now().format(DATE_FORMAT) + " | This is a computer-generated document.",
                SMALL_FONT);
        footer.setAlignment(Element.ALIGN_CENTER);
        footer.setSpacingBefore(20);
        document.add(footer);
    }

    // ===== Cell Helper Methods =====

    private void addTableHeaderCell(PdfPTable table, String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text, HEADER_FONT));
        cell.setBackgroundColor(PRIMARY_COLOR);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setPadding(6);
        cell.setBorderColor(PRIMARY_COLOR);
        table.addCell(cell);
    }

    private void addTableCell(PdfPTable table, String text, int alignment) {
        PdfPCell cell = new PdfPCell(new Phrase(text, BODY_FONT));
        cell.setHorizontalAlignment(alignment);
        cell.setPadding(5);
        cell.setBorderColor(BORDER_COLOR);
        table.addCell(cell);
    }

    private void addTableCellWithBackground(PdfPTable table, String text, int alignment, Color bgColor) {
        PdfPCell cell = new PdfPCell(new Phrase(text, BODY_FONT));
        cell.setHorizontalAlignment(alignment);
        cell.setPadding(5);
        cell.setBorderColor(BORDER_COLOR);
        cell.setBackgroundColor(bgColor);
        table.addCell(cell);
    }

    private void addLabelValueCell(PdfPTable table, String label, String value) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, BOLD_FONT));
        labelCell.setBorder(Rectangle.NO_BORDER);
        labelCell.setPadding(4);
        labelCell.setBackgroundColor(LIGHT_GRAY);
        table.addCell(labelCell);

        PdfPCell valueCell = new PdfPCell(new Phrase(value != null ? value : "N/A", BODY_FONT));
        valueCell.setBorder(Rectangle.NO_BORDER);
        valueCell.setPadding(4);
        table.addCell(valueCell);
    }

    private void addInfoCell(PdfPTable table, String label, String value) {
        PdfPCell cell = new PdfPCell();
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setPadding(3);
        Paragraph p = new Paragraph();
        p.add(new Chunk(label + " ", BOLD_FONT));
        p.add(new Chunk(value != null ? value : "N/A", BODY_FONT));
        cell.addElement(p);
        table.addCell(cell);
    }

    private void addSummaryCell(PdfPTable table, String label, String value) {
        PdfPCell cell = new PdfPCell();
        cell.setBorderColor(BORDER_COLOR);
        cell.setBorderWidth(1);
        cell.setPadding(8);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setBackgroundColor(LIGHT_GRAY);

        Paragraph labelPara = new Paragraph(label, SMALL_FONT);
        labelPara.setAlignment(Element.ALIGN_CENTER);
        cell.addElement(labelPara);

        Font valueFont = new Font(Font.HELVETICA, 14, Font.BOLD, PRIMARY_COLOR);
        Paragraph valuePara = new Paragraph(value, valueFont);
        valuePara.setAlignment(Element.ALIGN_CENTER);
        cell.addElement(valuePara);

        table.addCell(cell);
    }

    private void addSummaryRow(PdfPTable table, String label, String value) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, BOLD_FONT));
        labelCell.setBorder(Rectangle.TOP);
        labelCell.setBorderColor(BORDER_COLOR);
        labelCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        labelCell.setPadding(5);
        table.addCell(labelCell);

        PdfPCell valueCell = new PdfPCell(new Phrase(value, BOLD_FONT));
        valueCell.setBorder(Rectangle.TOP);
        valueCell.setBorderColor(BORDER_COLOR);
        valueCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        valueCell.setPadding(5);
        table.addCell(valueCell);
    }

    // ===== Utility Methods =====

    private String resolveClassName(String classId, String tenantId) {
        if (classId == null) return "N/A";
        try {
            UUID classUUID = UUID.fromString(classId);
            return schoolClassRepository.findByIdAndTenantId(classUUID, tenantId)
                    .map(SchoolClass::getName)
                    .orElse(classId);
        } catch (IllegalArgumentException e) {
            return classId;
        }
    }

    private String resolveSectionName(UUID sectionId) {
        if (sectionId == null) return "N/A";
        return sectionRepository.findById(sectionId)
                .map(Section::getName)
                .orElse("N/A");
    }

    private String formatAmount(BigDecimal amount, TenantSettings settings) {
        if (amount == null) return "0.00";
        String currency = (settings != null && settings.getCurrency() != null) ? settings.getCurrency() : "INR";
        String symbol = switch (currency) {
            case "USD" -> "$";
            case "EUR" -> "\u20AC";
            case "GBP" -> "\u00A3";
            default -> "\u20B9";
        };
        return symbol + " " + String.format("%,.2f", amount);
    }

    private String getGrade(double percentage) {
        if (percentage >= 90) return "A+";
        if (percentage >= 80) return "A";
        if (percentage >= 70) return "B+";
        if (percentage >= 60) return "B";
        if (percentage >= 50) return "C";
        if (percentage >= 40) return "D";
        if (percentage >= 33) return "E";
        return "F";
    }
}
