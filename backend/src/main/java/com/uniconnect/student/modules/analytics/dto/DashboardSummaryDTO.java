package com.uniconnect.student.modules.analytics.dto;

import java.util.List;
import java.util.Map;

/**
 * DTO for the dashboard summary containing all engagement metrics.
 */
public class DashboardSummaryDTO {

    private Integer totalActiveStudents;
    private Integer totalContributions;
    private Integer totalMentoringSessions;
    private List<TrendDataPoint> engagementTrend;
    private List<DepartmentMetric> departmentComparison;

    public DashboardSummaryDTO() {
    }

    public DashboardSummaryDTO(Integer totalActiveStudents, Integer totalContributions,
                                Integer totalMentoringSessions,
                                List<TrendDataPoint> engagementTrend,
                                List<DepartmentMetric> departmentComparison) {
        this.totalActiveStudents = totalActiveStudents;
        this.totalContributions = totalContributions;
        this.totalMentoringSessions = totalMentoringSessions;
        this.engagementTrend = engagementTrend;
        this.departmentComparison = departmentComparison;
    }

    // Getters and Setters

    public Integer getTotalActiveStudents() {
        return totalActiveStudents;
    }

    public void setTotalActiveStudents(Integer totalActiveStudents) {
        this.totalActiveStudents = totalActiveStudents;
    }

    public Integer getTotalContributions() {
        return totalContributions;
    }

    public void setTotalContributions(Integer totalContributions) {
        this.totalContributions = totalContributions;
    }

    public Integer getTotalMentoringSessions() {
        return totalMentoringSessions;
    }

    public void setTotalMentoringSessions(Integer totalMentoringSessions) {
        this.totalMentoringSessions = totalMentoringSessions;
    }

    public List<TrendDataPoint> getEngagementTrend() {
        return engagementTrend;
    }

    public void setEngagementTrend(List<TrendDataPoint> engagementTrend) {
        this.engagementTrend = engagementTrend;
    }

    public List<DepartmentMetric> getDepartmentComparison() {
        return departmentComparison;
    }

    public void setDepartmentComparison(List<DepartmentMetric> departmentComparison) {
        this.departmentComparison = departmentComparison;
    }

    @Override
    public String toString() {
        return "DashboardSummaryDTO{" +
                "totalActiveStudents=" + totalActiveStudents +
                ", totalContributions=" + totalContributions +
                ", totalMentoringSessions=" + totalMentoringSessions +
                ", engagementTrend=" + engagementTrend +
                ", departmentComparison=" + departmentComparison +
                '}';
    }

    /**
     * Represents a single data point in the engagement trend chart.
     */
    public static class TrendDataPoint {
        private String date;
        private Integer value;

        public TrendDataPoint() {
        }

        public TrendDataPoint(String date, Integer value) {
            this.date = date;
            this.value = value;
        }

        public String getDate() {
            return date;
        }

        public void setDate(String date) {
            this.date = date;
        }

        public Integer getValue() {
            return value;
        }

        public void setValue(Integer value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return "TrendDataPoint{date='" + date + "', value=" + value + "}";
        }
    }

    /**
     * Represents engagement metric data for a specific department.
     */
    public static class DepartmentMetric {
        private String department;
        private Integer value;

        public DepartmentMetric() {
        }

        public DepartmentMetric(String department, Integer value) {
            this.department = department;
            this.value = value;
        }

        public String getDepartment() {
            return department;
        }

        public void setDepartment(String department) {
            this.department = department;
        }

        public Integer getValue() {
            return value;
        }

        public void setValue(Integer value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return "DepartmentMetric{department='" + department + "', value=" + value + "}";
        }
    }
}
