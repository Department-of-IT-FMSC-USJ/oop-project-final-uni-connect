package com.app.modules.materials.enums;

/**
 * Enum representing the type of study material.
 */
public enum MaterialType {
    NOTES("Notes"),
    PAST_PAPERS("Past Papers"),
    LECTURE_SLIDES("Lecture Slides"),
    OTHER("Other");

    private final String displayName;

    MaterialType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
