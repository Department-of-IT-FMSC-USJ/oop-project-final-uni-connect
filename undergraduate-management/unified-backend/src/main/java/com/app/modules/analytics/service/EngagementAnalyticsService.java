package com.app.modules.analytics.service;

import com.app.modules.analytics.dto.DashboardSummaryDTO;
import com.app.modules.analytics.dto.EngagementAnalyticsResponseDTO;

import java.util.List;

/**
 * Service interface for Engagement Analytics operations.
 */
public interface EngagementAnalyticsService {

    /**
     * Get the dashboard summary with key metrics and chart data.
     *
     * @param activityFilter the activity type filter (CONTRIBUTIONS, MENTORING_SESSIONS, STUDY_MATERIAL_USAGE)
     * @param timeRange the time range filter (LAST_7_DAYS, LAST_30_DAYS, LAST_6_MONTHS)
     * @return the dashboard summary DTO
     */
    DashboardSummaryDTO getDashboardSummary(String activityFilter, String timeRange);

    /**
     * Get all analytics records.
     *
     * @return list of all analytics response DTOs
     */
    List<EngagementAnalyticsResponseDTO> getAllAnalytics();

    /**
     * Get analytics records filtered by department.
     *
     * @param department the department name
     * @return list of analytics response DTOs for the department
     */
    List<EngagementAnalyticsResponseDTO> getAnalyticsByDepartment(String department);

    /**
     * Record a new analytics metric.
     *
     * @param metricType the type of metric
     * @param metricValue the metric value
     * @param department the department
     * @return the saved analytics response DTO
     */
    EngagementAnalyticsResponseDTO recordMetric(String metricType, Integer metricValue, String department);
}
