package com.app.modules.analytics.serviceImpl;

import com.app.modules.analytics.dto.DashboardSummaryDTO;
import com.app.modules.analytics.dto.EngagementAnalyticsResponseDTO;
import com.app.modules.analytics.entity.EngagementAnalytics;
import com.app.modules.analytics.enums.MetricType;
import com.app.modules.analytics.repository.EngagementAnalyticsRepository;
import com.app.modules.analytics.service.EngagementAnalyticsService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of EngagementAnalyticsService.
 * Handles business logic for engagement analytics processing.
 */
@Service
public class EngagementAnalyticsServiceImpl implements EngagementAnalyticsService {

    private final EngagementAnalyticsRepository analyticsRepository;

    public EngagementAnalyticsServiceImpl(EngagementAnalyticsRepository analyticsRepository) {
        this.analyticsRepository = analyticsRepository;
    }

    /**
     * Get the dashboard summary with key metrics, trend data, and department comparison.
     */
    @Override
    public DashboardSummaryDTO getDashboardSummary(String activityFilter, String timeRange) {

        LocalDate endDate = LocalDate.now();
        LocalDate startDate = calculateStartDate(timeRange, endDate);

        // Get summary totals
        Integer totalActiveStudents = analyticsRepository.sumMetricValueByTypeAndDateRange(
                MetricType.ACTIVE_STUDENTS, startDate, endDate);

        Integer totalContributions = analyticsRepository.sumMetricValueByTypeAndDateRange(
                MetricType.CONTRIBUTIONS_SUBMITTED, startDate, endDate);

        Integer totalMentoringSessions = analyticsRepository.sumMetricValueByTypeAndDateRange(
                MetricType.MENTORING_SESSIONS, startDate, endDate);

        // Determine which metric type to use for trend and department data
        MetricType trendMetricType = mapActivityFilterToMetricType(activityFilter);

        // Get engagement trend data
        List<EngagementAnalytics> trendRecords =
                analyticsRepository.findByMetricTypeAndRecordedDateBetweenOrderByRecordedDateAsc(
                        trendMetricType, startDate, endDate);

        List<DashboardSummaryDTO.TrendDataPoint> trendData = trendRecords.stream()
                .map(record -> new DashboardSummaryDTO.TrendDataPoint(
                        record.getRecordedDate().toString(),
                        record.getMetricValue()))
                .collect(Collectors.toList());

        // Get department comparison data
        List<Object[]> departmentResults = analyticsRepository.findDepartmentSummary(
                trendMetricType, startDate, endDate);

        List<DashboardSummaryDTO.DepartmentMetric> departmentData = new ArrayList<>();
        for (Object[] row : departmentResults) {
            String department = (String) row[0];
            Long value = (Long) row[1];
            departmentData.add(new DashboardSummaryDTO.DepartmentMetric(
                    department, value.intValue()));
        }

        // Build and return dashboard summary
        DashboardSummaryDTO summary = new DashboardSummaryDTO();
        summary.setTotalActiveStudents(totalActiveStudents);
        summary.setTotalContributions(totalContributions);
        summary.setTotalMentoringSessions(totalMentoringSessions);
        summary.setEngagementTrend(trendData);
        summary.setDepartmentComparison(departmentData);

        return summary;
    }

    /**
     * Get all analytics records.
     */
    @Override
    public List<EngagementAnalyticsResponseDTO> getAllAnalytics() {
        List<EngagementAnalytics> records = analyticsRepository.findAll();

        return records.stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get analytics records filtered by department.
     */
    @Override
    public List<EngagementAnalyticsResponseDTO> getAnalyticsByDepartment(String department) {
        List<EngagementAnalytics> records =
                analyticsRepository.findByDepartmentOrderByRecordedDateDesc(department);

        return records.stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Record a new analytics metric.
     */
    @Override
    public EngagementAnalyticsResponseDTO recordMetric(String metricType, Integer metricValue,
                                                        String department) {
        MetricType type = MetricType.valueOf(metricType);

        EngagementAnalytics analytics = new EngagementAnalytics();
        analytics.setMetricType(type);
        analytics.setMetricValue(metricValue);
        analytics.setDepartment(department);

        EngagementAnalytics saved = analyticsRepository.save(analytics);

        return mapToResponseDTO(saved);
    }

    /**
     * Calculate the start date based on the time range filter.
     */
    private LocalDate calculateStartDate(String timeRange, LocalDate endDate) {
        if (timeRange == null) {
            return endDate.minusDays(30);
        }

        switch (timeRange) {
            case "LAST_7_DAYS":
                return endDate.minusDays(7);
            case "LAST_6_MONTHS":
                return endDate.minusMonths(6);
            case "LAST_30_DAYS":
            default:
                return endDate.minusDays(30);
        }
    }

    /**
     * Map the activity filter string to the corresponding MetricType enum.
     */
    private MetricType mapActivityFilterToMetricType(String activityFilter) {
        if (activityFilter == null) {
            return MetricType.CONTRIBUTIONS_SUBMITTED;
        }

        switch (activityFilter) {
            case "MENTORING_SESSIONS":
                return MetricType.MENTORING_SESSIONS;
            case "STUDY_MATERIAL_USAGE":
                return MetricType.STUDY_MATERIAL_USAGE;
            case "CONTRIBUTIONS":
            default:
                return MetricType.CONTRIBUTIONS_SUBMITTED;
        }
    }

    /**
     * Map EngagementAnalytics entity to EngagementAnalyticsResponseDTO.
     */
    private EngagementAnalyticsResponseDTO mapToResponseDTO(EngagementAnalytics analytics) {
        return EngagementAnalyticsResponseDTO.builder()
                .analyticsId(analytics.getAnalyticsId())
                .metricType(analytics.getMetricType().getDisplayName())
                .metricValue(analytics.getMetricValue())
                .department(analytics.getDepartment())
                .recordedDate(analytics.getRecordedDate())
                .calculatedBy(analytics.getCalculatedBy())
                .build();
    }
}
