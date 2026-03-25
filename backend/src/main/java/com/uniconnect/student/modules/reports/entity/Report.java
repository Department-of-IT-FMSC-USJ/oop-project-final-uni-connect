package com.uniconnect.student.modules.reports.entity;

import com.uniconnect.student.modules.reports.enums.ReportFormat;
import com.uniconnect.student.modules.reports.enums.ReportType;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entity class mapped to the 'reports' database table.
 */
@Entity
@Table(name = "reports")
public class Report {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "report_id")
    private Integer reportId;

    @Column(name = "generated_by", nullable = false)
    private Integer generatedBy;

    @Enumerated(EnumType.STRING)
    @Column(name = "report_type", nullable = false, length = 50)
    private ReportType reportType;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "department", length = 100)
    private String department;

    @Enumerated(EnumType.STRING)
    @Column(name = "report_format", nullable = false, length = 10)
    private ReportFormat reportFormat;

    @Column(name = "file_path", length = 255)
    private String filePath;

    @Column(name = "generated_date", nullable = false)
    private LocalDateTime generatedDate;

    public Report() {
    }

    public Report(Integer reportId, Integer generatedBy, ReportType reportType,
                  LocalDate startDate, LocalDate endDate, String department,
                  ReportFormat reportFormat, String filePath, LocalDateTime generatedDate) {
        this.reportId = reportId;
        this.generatedBy = generatedBy;
        this.reportType = reportType;
        this.startDate = startDate;
        this.endDate = endDate;
        this.department = department;
        this.reportFormat = reportFormat;
        this.filePath = filePath;
        this.generatedDate = generatedDate;
    }

    @PrePersist
    protected void onCreate() {
        this.generatedDate = LocalDateTime.now();
    }

    // Getters and Setters

    public Integer getReportId() {
        return reportId;
    }

    public void setReportId(Integer reportId) {
        this.reportId = reportId;
    }

    public Integer getGeneratedBy() {
        return generatedBy;
    }

    public void setGeneratedBy(Integer generatedBy) {
        this.generatedBy = generatedBy;
    }

    public ReportType getReportType() {
        return reportType;
    }

    public void setReportType(ReportType reportType) {
        this.reportType = reportType;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public ReportFormat getReportFormat() {
        return reportFormat;
    }

    public void setReportFormat(ReportFormat reportFormat) {
        this.reportFormat = reportFormat;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public LocalDateTime getGeneratedDate() {
        return generatedDate;
    }

    public void setGeneratedDate(LocalDateTime generatedDate) {
        this.generatedDate = generatedDate;
    }

    @Override
    public String toString() {
        return "Report{" +
                "reportId=" + reportId +
                ", generatedBy=" + generatedBy +
                ", reportType=" + reportType +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                ", department='" + department + '\'' +
                ", reportFormat=" + reportFormat +
                ", filePath='" + filePath + '\'' +
                ", generatedDate=" + generatedDate +
                '}';
    }
}
