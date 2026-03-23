package com.uniconnect.student.modules.analytics.dto;

import java.time.LocalDate;

/**
 * DTO for engagement analytics response.
 */
public class EngagementAnalyticsResponseDTO {

    private Integer analyticsId;
    private String metricType;
    private Integer metricValue;
    private String department;
    private LocalDate recordedDate;
    private String calculatedBy;

    public EngagementAnalyticsResponseDTO() {
    }

    public EngagementAnalyticsResponseDTO(Integer analyticsId, String metricType, Integer metricValue,
                                           String department, LocalDate recordedDate, String calculatedBy) {
        this.analyticsId = analyticsId;
        this.metricType = metricType;
        this.metricValue = metricValue;
        this.department = department;
        this.recordedDate = recordedDate;
        this.calculatedBy = calculatedBy;
    }

    // Getters and Setters

    public Integer getAnalyticsId() {
        return analyticsId;
    }

    public void setAnalyticsId(Integer analyticsId) {
        this.analyticsId = analyticsId;
    }

    public String getMetricType() {
        return metricType;
    }

    public void setMetricType(String metricType) {
        this.metricType = metricType;
    }

    public Integer getMetricValue() {
        return metricValue;
    }

    public void setMetricValue(Integer metricValue) {
        this.metricValue = metricValue;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public LocalDate getRecordedDate() {
        return recordedDate;
    }

    public void setRecordedDate(LocalDate recordedDate) {
        this.recordedDate = recordedDate;
    }

    public String getCalculatedBy() {
        return calculatedBy;
    }

    public void setCalculatedBy(String calculatedBy) {
        this.calculatedBy = calculatedBy;
    }

    @Override
    public String toString() {
        return "EngagementAnalyticsResponseDTO{" +
                "analyticsId=" + analyticsId +
                ", metricType='" + metricType + '\'' +
                ", metricValue=" + metricValue +
                ", department='" + department + '\'' +
                ", recordedDate=" + recordedDate +
                ", calculatedBy='" + calculatedBy + '\'' +
                '}';
    }

    // Static Builder

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Integer analyticsId;
        private String metricType;
        private Integer metricValue;
        private String department;
        private LocalDate recordedDate;
        private String calculatedBy;

        public Builder analyticsId(Integer analyticsId) {
            this.analyticsId = analyticsId;
            return this;
        }

        public Builder metricType(String metricType) {
            this.metricType = metricType;
            return this;
        }

        public Builder metricValue(Integer metricValue) {
            this.metricValue = metricValue;
            return this;
        }

        public Builder department(String department) {
            this.department = department;
            return this;
        }

        public Builder recordedDate(LocalDate recordedDate) {
            this.recordedDate = recordedDate;
            return this;
        }

        public Builder calculatedBy(String calculatedBy) {
            this.calculatedBy = calculatedBy;
            return this;
        }

        public EngagementAnalyticsResponseDTO build() {
            return new EngagementAnalyticsResponseDTO(
                    analyticsId, metricType, metricValue,
                    department, recordedDate, calculatedBy);
        }
    }
}
