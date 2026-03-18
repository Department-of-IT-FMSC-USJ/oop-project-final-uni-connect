package com.app.modules.reports.enums;

/**
 * Enum representing the output format of a generated report.
 */
public enum ReportFormat {

    PDF("PDF"),
    EXCEL("Excel");

    private final String displayName;

    ReportFormat(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
