package com.uniconnect.student.modules.analytics.controller;

import com.uniconnect.student.common.dto.ApiResponseDTO;
import com.uniconnect.student.modules.analytics.dto.DashboardSummaryDTO;
import com.uniconnect.student.modules.analytics.dto.EngagementAnalyticsResponseDTO;
import com.uniconnect.student.modules.analytics.service.EngagementAnalyticsService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for Engagement Analytics operations.
 * Provides endpoints for retrieving analytics dashboard data and recording metrics.
 */
@RestController
@RequestMapping("/api/analytics")
public class EngagementAnalyticsController {

    private final EngagementAnalyticsService analyticsService;

    public EngagementAnalyticsController(EngagementAnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    /**
     * Get the dashboard summary with key metrics, trend chart data, and department comparison.
     *
     * GET /api/analytics/dashboard?activityFilter=CONTRIBUTIONS&timeRange=LAST_30_DAYS
     *
     * @param activityFilter optional activity type filter (CONTRIBUTIONS, MENTORING_SESSIONS, STUDY_MATERIAL_USAGE)
     * @param timeRange optional time range filter (LAST_7_DAYS, LAST_30_DAYS, LAST_6_MONTHS)
     * @return the dashboard summary
     */
    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponseDTO<DashboardSummaryDTO>> getDashboardSummary(
            @RequestParam(value = "activityFilter", required = false) String activityFilter,
            @RequestParam(value = "timeRange", required = false) String timeRange) {

        DashboardSummaryDTO summary = analyticsService.getDashboardSummary(activityFilter, timeRange);

        ApiResponseDTO<DashboardSummaryDTO> response = ApiResponseDTO.success(
                "Dashboard summary retrieved successfully", summary);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Get all analytics records.
     *
     * GET /api/analytics
     *
     * @return list of all analytics records
     */
    @GetMapping
    public ResponseEntity<ApiResponseDTO<List<EngagementAnalyticsResponseDTO>>> getAllAnalytics() {

        List<EngagementAnalyticsResponseDTO> analytics = analyticsService.getAllAnalytics();

        ApiResponseDTO<List<EngagementAnalyticsResponseDTO>> response = ApiResponseDTO.success(
                "Analytics records retrieved successfully", analytics);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Get analytics records for a specific department.
     *
     * GET /api/analytics/department/{department}
     *
     * @param department the department name
     * @return list of analytics records for the department
     */
    @GetMapping("/department/{department}")
    public ResponseEntity<ApiResponseDTO<List<EngagementAnalyticsResponseDTO>>> getAnalyticsByDepartment(
            @PathVariable("department") String department) {

        List<EngagementAnalyticsResponseDTO> analytics =
                analyticsService.getAnalyticsByDepartment(department);

        ApiResponseDTO<List<EngagementAnalyticsResponseDTO>> response = ApiResponseDTO.success(
                "Department analytics retrieved successfully", analytics);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Record a new analytics metric.
     *
     * POST /api/analytics/record?metricType=ACTIVE_STUDENTS&metricValue=150&department=Computer Science
     *
     * @param metricType the type of metric to record
     * @param metricValue the metric value
     * @param department the department
     * @return the recorded analytics entry
     */
    @PostMapping("/record")
    public ResponseEntity<ApiResponseDTO<EngagementAnalyticsResponseDTO>> recordMetric(
            @RequestParam("metricType") String metricType,
            @RequestParam("metricValue") Integer metricValue,
            @RequestParam(value = "department", required = false) String department) {

        EngagementAnalyticsResponseDTO responseDTO =
                analyticsService.recordMetric(metricType, metricValue, department);

        ApiResponseDTO<EngagementAnalyticsResponseDTO> response = ApiResponseDTO.success(
                "Metric recorded successfully", responseDTO);

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
}
