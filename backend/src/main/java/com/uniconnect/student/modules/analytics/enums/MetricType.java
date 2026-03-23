package com.uniconnect.student.modules.analytics.enums;

/**
 * Enum representing the types of engagement metrics tracked in the system.
 */
public enum MetricType {

    ACTIVE_STUDENTS("Active Students"),
    CONTRIBUTIONS_SUBMITTED("Contributions Submitted"),
    MENTORING_SESSIONS("Mentoring Sessions Conducted"),
    STUDY_MATERIAL_USAGE("Study Material Usage"),
    EVENT_PARTICIPATION("Event Participation");

    private final String displayName;

    MetricType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
