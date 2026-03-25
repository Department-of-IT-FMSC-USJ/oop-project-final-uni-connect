package com.uniconnect.student.modules.points.enums;

/**
 * Enum representing the type of activity that earns contribution points.
 */
public enum ActivityType {
    EVENT_PARTICIPATION("Event Participation", 10),
    EVENT_ORGANIZING("Event Organizing", 20),
    EVENT_WINNING("Event Winning", 30),
    COMMUNITY_SERVICE("Community Service", 15),
    ACADEMIC_ACHIEVEMENT("Academic Achievement", 25);

    private final String displayName;
    private final int defaultPoints;

    ActivityType(String displayName, int defaultPoints) {
        this.displayName = displayName;
        this.defaultPoints = defaultPoints;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getDefaultPoints() {
        return defaultPoints;
    }
}
