package com.uniconnect.student.modules.reports.controller;

import com.uniconnect.model.User;
import com.uniconnect.student.common.dto.ApiResponseDTO;
import com.uniconnect.student.modules.reports.dto.ReportRequestDTO;
import com.uniconnect.student.modules.reports.dto.ReportResponseDTO;
import com.uniconnect.student.modules.reports.dto.SessionReportResponseDTO;
import com.uniconnect.student.modules.reports.service.ReportService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/hod/session-report")
    public ResponseEntity<ApiResponseDTO<SessionReportResponseDTO>> getHodSessionReport(
            @AuthenticationPrincipal User currentUser,
            @RequestParam String startDate,
            @RequestParam String endDate,
            @RequestParam(required = false, defaultValue = "ALL") String mentorType) {

        LocalDate start = LocalDate.parse(startDate);
        LocalDate end = LocalDate.parse(endDate);

        SessionReportResponseDTO report = reportService.generateHodSessionReport(
                currentUser.getId(), start, end, mentorType);

        return ResponseEntity.ok(ApiResponseDTO.success("Report generated successfully", report));
    }

    @GetMapping("/mentor/academic/session-report")
    public ResponseEntity<ApiResponseDTO<SessionReportResponseDTO>> getAcademicMentorReport(
            @RequestHeader("X-User-Id") Integer mentorId,
            @RequestParam String startDate,
            @RequestParam String endDate) {

        LocalDate start = LocalDate.parse(startDate);
        LocalDate end = LocalDate.parse(endDate);

        SessionReportResponseDTO report = reportService.generateMentorSessionReport(
                mentorId, "ACADEMIC", start, end);

        return ResponseEntity.ok(ApiResponseDTO.success("Report generated successfully", report));
    }

    @GetMapping("/mentor/industry/session-report")
    public ResponseEntity<ApiResponseDTO<SessionReportResponseDTO>> getIndustryMentorReport(
            @RequestHeader("X-User-Id") Integer mentorId,
            @RequestParam String startDate,
            @RequestParam String endDate) {

        LocalDate start = LocalDate.parse(startDate);
        LocalDate end = LocalDate.parse(endDate);

        SessionReportResponseDTO report = reportService.generateMentorSessionReport(
                mentorId, "INDUSTRY", start, end);

        return ResponseEntity.ok(ApiResponseDTO.success("Report generated successfully", report));
    }

    @PostMapping("/generate")
    public ResponseEntity<ApiResponseDTO<ReportResponseDTO>> generateReport(
            @Valid @RequestBody ReportRequestDTO requestDTO) {
        ReportResponseDTO responseDTO = reportService.generateReport(requestDTO);
        return new ResponseEntity<>(ApiResponseDTO.success("Report generated successfully", responseDTO), HttpStatus.CREATED);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponseDTO<List<ReportResponseDTO>>> getReportsByUser(
            @PathVariable("userId") Integer userId) {
        List<ReportResponseDTO> reports = reportService.getReportsByUser(userId);
        return ResponseEntity.ok(ApiResponseDTO.success("Reports retrieved successfully", reports));
    }

    @GetMapping("/{reportId}")
    public ResponseEntity<ApiResponseDTO<ReportResponseDTO>> getReportById(
            @PathVariable("reportId") Integer reportId) {
        ReportResponseDTO responseDTO = reportService.getReportById(reportId);
        return ResponseEntity.ok(ApiResponseDTO.success("Report retrieved successfully", responseDTO));
    }

    @GetMapping
    public ResponseEntity<ApiResponseDTO<List<ReportResponseDTO>>> getAllReports() {
        List<ReportResponseDTO> reports = reportService.getAllReports();
        return ResponseEntity.ok(ApiResponseDTO.success("All reports retrieved successfully", reports));
    }
}
