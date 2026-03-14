package com.app.modules.reports.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO for report generation response.
 */
public class ReportResponseDTO {

    private Integer reportId;
    private Integer generatedBy;
    private String reportType;
    private LocalDate startDate;
    private LocalDate endDate;
    private String department;
    private String reportFormat;
    private String filePath;
    private LocalDateTime generatedDate;

    public ReportResponseDTO() {
    }

    public ReportResponseDTO(Integer reportId, Integer generatedBy, String reportType,
                              LocalDate startDate, LocalDate endDate, String department,
                              String reportFormat, String filePath, LocalDateTime generatedDate) {
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

    public String getReportType() {
        return reportType;
    }

    public void setReportType(String reportType) {
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

    public String getReportFormat() {
        return reportFormat;
    }

    public void setReportFormat(String reportFormat) {
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
        return "ReportResponseDTO{" +
                "reportId=" + reportId +
                ", generatedBy=" + generatedBy +
                ", reportType='" + reportType + '\'' +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                ", department='" + department + '\'' +
                ", reportFormat='" + reportFormat + '\'' +
                ", filePath='" + filePath + '\'' +
                ", generatedDate=" + generatedDate +
                '}';
    }

    // Static Builder

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Integer reportId;
        private Integer generatedBy;
        private String reportType;
        private LocalDate startDate;
        private LocalDate endDate;
        private String department;
        private String reportFormat;
        private String filePath;
        private LocalDateTime generatedDate;

        public Builder reportId(Integer reportId) {
            this.reportId = reportId;
            return this;
        }

        public Builder generatedBy(Integer generatedBy) {
            this.generatedBy = generatedBy;
            return this;
        }

        public Builder reportType(String reportType) {
            this.reportType = reportType;
            return this;
        }

        public Builder startDate(LocalDate startDate) {
            this.startDate = startDate;
            return this;
        }

        public Builder endDate(LocalDate endDate) {
            this.endDate = endDate;
            return this;
        }

        public Builder department(String department) {
            this.department = department;
            return this;
        }

        public Builder reportFormat(String reportFormat) {
            this.reportFormat = reportFormat;
            return this;
        }

        public Builder filePath(String filePath) {
            this.filePath = filePath;
            return this;
        }

        public Builder generatedDate(LocalDateTime generatedDate) {
            this.generatedDate = generatedDate;
            return this;
        }

        public ReportResponseDTO build() {
            return new ReportResponseDTO(
                    reportId, generatedBy, reportType, startDate, endDate,
                    department, reportFormat, filePath, generatedDate);
        }
    }
}
