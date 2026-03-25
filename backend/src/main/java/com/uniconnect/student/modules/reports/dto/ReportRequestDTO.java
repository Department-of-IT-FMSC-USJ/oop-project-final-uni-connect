package com.uniconnect.student.modules.reports.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

/**
 * DTO for report generation request.
 */
public class ReportRequestDTO {

    @NotNull(message = "User ID is required")
    private Integer userId;

    @NotBlank(message = "Report type is required")
    @Pattern(regexp = "STUDENT_CONTRIBUTION|MENTOR_ACTIVITY|MENTORING_SESSION|ENGAGEMENT_SUMMARY",
            message = "Invalid report type")
    private String reportType;

    @NotBlank(message = "Start date is required")
    private String startDate;

    @NotBlank(message = "End date is required")
    private String endDate;

    private String department;

    @NotBlank(message = "Report format is required")
    @Pattern(regexp = "PDF|EXCEL", message = "Report format must be PDF or EXCEL")
    private String reportFormat;

    public ReportRequestDTO() {
    }

    public ReportRequestDTO(Integer userId, String reportType, String startDate,
                            String endDate, String department, String reportFormat) {
        this.userId = userId;
        this.reportType = reportType;
        this.startDate = startDate;
        this.endDate = endDate;
        this.department = department;
        this.reportFormat = reportFormat;
    }

    // Getters and Setters

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getReportType() {
        return reportType;
    }

    public void setReportType(String reportType) {
        this.reportType = reportType;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
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

    @Override
    public String toString() {
        return "ReportRequestDTO{" +
                "userId=" + userId +
                ", reportType='" + reportType + '\'' +
                ", startDate='" + startDate + '\'' +
                ", endDate='" + endDate + '\'' +
                ", department='" + department + '\'' +
                ", reportFormat='" + reportFormat + '\'' +
                '}';
    }
}
