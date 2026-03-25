package com.uniconnect.student.modules.reports.dto;

import java.util.List;

public class SessionReportResponseDTO {

    private String reportTitle;
    private String generatedBy;
    private String department;
    private String dateRange;
    private String generatedAt;
    private int totalSessions;
    private int totalParticipants;
    private List<SessionReportDTO> sessions;

    public SessionReportResponseDTO() {}

    public String getReportTitle() { return reportTitle; }
    public void setReportTitle(String reportTitle) { this.reportTitle = reportTitle; }

    public String getGeneratedBy() { return generatedBy; }
    public void setGeneratedBy(String generatedBy) { this.generatedBy = generatedBy; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public String getDateRange() { return dateRange; }
    public void setDateRange(String dateRange) { this.dateRange = dateRange; }

    public String getGeneratedAt() { return generatedAt; }
    public void setGeneratedAt(String generatedAt) { this.generatedAt = generatedAt; }

    public int getTotalSessions() { return totalSessions; }
    public void setTotalSessions(int totalSessions) { this.totalSessions = totalSessions; }

    public int getTotalParticipants() { return totalParticipants; }
    public void setTotalParticipants(int totalParticipants) { this.totalParticipants = totalParticipants; }

    public List<SessionReportDTO> getSessions() { return sessions; }
    public void setSessions(List<SessionReportDTO> sessions) { this.sessions = sessions; }
}
