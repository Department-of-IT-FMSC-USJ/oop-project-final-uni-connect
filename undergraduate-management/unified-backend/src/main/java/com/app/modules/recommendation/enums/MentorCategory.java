package com.app.modules.recommendation.enums;

/**
 * Enum representing the category of a mentor.
 */
public enum MentorCategory {

    ACADEMIC_MENTOR("Academic Mentor"),
    INDUSTRY_MENTOR("Industry Mentor");

    private final String displayName;

    MentorCategory(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
