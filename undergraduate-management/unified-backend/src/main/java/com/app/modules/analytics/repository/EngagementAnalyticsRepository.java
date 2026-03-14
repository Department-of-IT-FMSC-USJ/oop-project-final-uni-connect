package com.app.modules.analytics.repository;

import com.app.modules.analytics.entity.EngagementAnalytics;
import com.app.modules.analytics.enums.MetricType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * Repository interface for EngagementAnalytics entity.
 */
@Repository
public interface EngagementAnalyticsRepository extends JpaRepository<EngagementAnalytics, Integer> {

    /**
     * Find all analytics records by metric type, ordered by recorded date descending.
     */
    List<EngagementAnalytics> findByMetricTypeOrderByRecordedDateDesc(MetricType metricType);

    /**
     * Find all analytics records by department, ordered by recorded date descending.
     */
    List<EngagementAnalytics> findByDepartmentOrderByRecordedDateDesc(String department);

    /**
     * Find analytics records within a date range for a specific metric type.
     */
    List<EngagementAnalytics> findByMetricTypeAndRecordedDateBetweenOrderByRecordedDateAsc(
            MetricType metricType, LocalDate startDate, LocalDate endDate);

    /**
     * Find analytics records within a date range.
     */
    List<EngagementAnalytics> findByRecordedDateBetweenOrderByRecordedDateAsc(
            LocalDate startDate, LocalDate endDate);

    /**
     * Get the sum of metric values grouped by department for a specific metric type.
     */
    @Query("SELECT e.department, SUM(e.metricValue) FROM EngagementAnalytics e " +
            "WHERE e.metricType = :metricType AND e.recordedDate BETWEEN :startDate AND :endDate " +
            "GROUP BY e.department ORDER BY SUM(e.metricValue) DESC")
    List<Object[]> findDepartmentSummary(@Param("metricType") MetricType metricType,
                                          @Param("startDate") LocalDate startDate,
                                          @Param("endDate") LocalDate endDate);

    /**
     * Get the latest metric value for a given metric type.
     */
    @Query("SELECT COALESCE(SUM(e.metricValue), 0) FROM EngagementAnalytics e " +
            "WHERE e.metricType = :metricType AND e.recordedDate BETWEEN :startDate AND :endDate")
    Integer sumMetricValueByTypeAndDateRange(@Param("metricType") MetricType metricType,
                                              @Param("startDate") LocalDate startDate,
                                              @Param("endDate") LocalDate endDate);
}
