package com.uniconnect.student.modules.reports.service;

import com.uniconnect.student.modules.reports.dto.ReportRequestDTO;
import com.uniconnect.student.modules.reports.dto.ReportResponseDTO;
import com.uniconnect.student.modules.reports.dto.SessionReportResponseDTO;

import java.time.LocalDate;
import java.util.List;

public interface ReportService {

    ReportResponseDTO generateReport(ReportRequestDTO requestDTO);

    List<ReportResponseDTO> getReportsByUser(Integer userId);

    ReportResponseDTO getReportById(Integer reportId);

    List<ReportResponseDTO> getAllReports();

    SessionReportResponseDTO generateHodSessionReport(Long hodUserId, LocalDate startDate, LocalDate endDate, String mentorTypeFilter);

    SessionReportResponseDTO generateMentorSessionReport(Integer mentorId, String mentorType, LocalDate startDate, LocalDate endDate);
}
