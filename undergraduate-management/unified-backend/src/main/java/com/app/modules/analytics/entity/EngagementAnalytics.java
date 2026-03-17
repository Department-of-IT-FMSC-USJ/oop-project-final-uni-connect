package com.app.modules.analytics.entity;

import com.app.modules.analytics.enums.MetricType;
import jakarta.persistence.*;

import java.time.LocalDate;

/**
 * Entity class mapped to the 'engagement_analytics' database table.
 */
@Entity
@Table(name = "engagement_analytics")
public class EngagementAnalytics {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "analytics_id")
    private Integer analyticsId;

    @Enumerated(EnumType.STRING)
    @Column(name = "metric_type", nullable = false, length = 50)
    private MetricType metricType;

    @Column(name = "metric_value", nullable = false)
    private Integer metricValue;

    @Column(name = "department", length = 100)
    private String department;

    @Column(name = "recorded_date", nullable = false)
    private LocalDate recordedDate;

    @Column(name = "calculated_by", length = 50)
    private String calculatedBy;

    public EngagementAnalytics() {
    }

    public EngagementAnalytics(Integer analyticsId, MetricType metricType, Integer metricValue,
                                String department, LocalDate recordedDate, String calculatedBy) {
        this.analyticsId = analyticsId;
        this.metricType = metricType;
        this.metricValue = metricValue;
        this.department = department;
        this.recordedDate = recordedDate;
        this.calculatedBy = calculatedBy;
    }

    @PrePersist
    protected void onCreate() {
        if (this.recordedDate == null) {
            this.recordedDate = LocalDate.now();
        }
        if (this.calculatedBy == null) {
            this.calculatedBy = "SYSTEM";
        }
    }

    // Getters and Setters

    public Integer getAnalyticsId() {
        return analyticsId;
    }

    public void setAnalyticsId(Integer analyticsId) {
        this.analyticsId = analyticsId;
    }

    public MetricType getMetricType() {
        return metricType;
    }

    public void setMetricType(MetricType metricType) {
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
        return "EngagementAnalytics{" +
                "analyticsId=" + analyticsId +
                ", metricType=" + metricType +
                ", metricValue=" + metricValue +
                ", department='" + department + '\'' +
                ", recordedDate=" + recordedDate +
                ", calculatedBy='" + calculatedBy + '\'' +
                '}';
    }
}
