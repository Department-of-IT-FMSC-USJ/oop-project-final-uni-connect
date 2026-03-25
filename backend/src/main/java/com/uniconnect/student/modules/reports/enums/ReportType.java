package com.uniconnect.student.modules.reports.enums;

/**
 * Enum representing the types of reports that can be generated.
 */
public enum ReportType {

    STUDENT_CONTRIBUTION("Student Contribution Report"),
    MENTOR_ACTIVITY("Mentor Activity Report"),
    MENTORING_SESSION("Mentoring Session Report"),
    ENGAGEMENT_SUMMARY("Engagement Summary");

    private final String displayName;

    ReportType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
