package com.uniconnect.student.modules.reports.serviceImpl;

import com.uniconnect.model.Role;
import com.uniconnect.model.User;
import com.uniconnect.repository.UserRepository;
import com.uniconnect.student.common.exception.ResourceNotFoundException;
import com.uniconnect.student.modules.reports.dto.*;
import com.uniconnect.student.modules.reports.entity.Report;
import com.uniconnect.student.modules.reports.enums.ReportFormat;
import com.uniconnect.student.modules.reports.enums.ReportType;
import com.uniconnect.student.modules.reports.exception.InvalidDateRangeException;
import com.uniconnect.student.modules.reports.exception.ReportGenerationException;
import com.uniconnect.student.modules.reports.repository.ReportRepository;
import com.uniconnect.student.modules.reports.service.ReportService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ReportServiceImpl implements ReportService {

    private final ReportRepository reportRepository;
    private final UserRepository userRepository;
    private final com.uniconnect.academic.modules.sessions.repository.MentoringSessionRepository academicSessionRepo;
    private final com.uniconnect.industry.modules.sessions.repository.MentoringSessionRepository industrySessionRepo;

    @Value("${file.reports-dir:uploads/reports}")
    private String reportsDir;

    public ReportServiceImpl(
            ReportRepository reportRepository,
            UserRepository userRepository,
            @Qualifier("academicMentoringSessionRepository")
            com.uniconnect.academic.modules.sessions.repository.MentoringSessionRepository academicSessionRepo,
            @Qualifier("industryMentoringSessionRepository")
            com.uniconnect.industry.modules.sessions.repository.MentoringSessionRepository industrySessionRepo) {
        this.reportRepository = reportRepository;
        this.userRepository = userRepository;
        this.academicSessionRepo = academicSessionRepo;
        this.industrySessionRepo = industrySessionRepo;
    }

    @Override
    public SessionReportResponseDTO generateHodSessionReport(Long hodUserId, LocalDate startDate, LocalDate endDate, String mentorTypeFilter) {
        if (endDate.isBefore(startDate)) {
            throw new InvalidDateRangeException("End date must not be before start date");
        }

        User hodUser = userRepository.findById(hodUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + hodUserId));

        String department = hodUser.getDepartment();
        List<SessionReportDTO> allSessionDTOs = new ArrayList<>();

        if (!"INDUSTRY".equalsIgnoreCase(mentorTypeFilter)) {
            List<User> academicMentors = userRepository.findByRoleAndDepartmentAndActiveTrue(Role.ACADEMIC_MENTOR, department);
            for (User mentor : academicMentors) {
                var sessions = academicSessionRepo.findByMentorIdAndSessionDateBetweenOrderBySessionDateDescSessionTimeDesc(
                        mentor.getId().intValue(), startDate, endDate);
                for (var session : sessions) {
                    allSessionDTOs.add(buildSessionReportDTO(session.getSessionId(), session.getSessionTitle(),
                            session.getSessionTopic(), session.getSessionType() != null ? session.getSessionType().name() : null,
                            mentor.getFullName(), "Academic", session.getSessionDate(), session.getSessionTime(),
                            session.getAudienceMode(), session.getTargetStudentIds()));
                }
            }
        }

        if (!"ACADEMIC".equalsIgnoreCase(mentorTypeFilter)) {
            List<User> industryMentors = userRepository.findByRoleAndDepartmentAndActiveTrue(Role.INDUSTRY_MENTOR, department);
            for (User mentor : industryMentors) {
                var sessions = industrySessionRepo.findByMentorIdAndSessionDateBetweenOrderBySessionDateDescSessionTimeDesc(
                        mentor.getId().intValue(), startDate, endDate);
                for (var session : sessions) {
                    allSessionDTOs.add(buildSessionReportDTO(session.getSessionId(), session.getSessionTitle(),
                            session.getSessionTopic(), session.getSessionType() != null ? session.getSessionType().name() : null,
                            mentor.getFullName(), "Industry", session.getSessionDate(), session.getSessionTime(),
                            session.getAudienceMode(), session.getTargetStudentIds()));
                }
            }
        }

        allSessionDTOs.sort((a, b) -> {
            String dateA = a.getSessionDate() != null ? a.getSessionDate() : "";
            String dateB = b.getSessionDate() != null ? b.getSessionDate() : "";
            return dateB.compareTo(dateA);
        });

        int totalParticipants = allSessionDTOs.stream().mapToInt(SessionReportDTO::getParticipantCount).sum();

        SessionReportResponseDTO response = new SessionReportResponseDTO();
        response.setReportTitle("Department Session Report");
        response.setGeneratedBy(hodUser.getFullName());
        response.setDepartment(department);
        response.setDateRange(startDate.toString() + " to " + endDate.toString());
        response.setGeneratedAt(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        response.setTotalSessions(allSessionDTOs.size());
        response.setTotalParticipants(totalParticipants);
        response.setSessions(allSessionDTOs);

        return response;
    }

    @Override
    public SessionReportResponseDTO generateMentorSessionReport(Integer mentorId, String mentorType, LocalDate startDate, LocalDate endDate) {
        if (endDate.isBefore(startDate)) {
            throw new InvalidDateRangeException("End date must not be before start date");
        }

        User mentor = userRepository.findById(mentorId.longValue())
                .orElseThrow(() -> new ResourceNotFoundException("Mentor not found: " + mentorId));

        List<SessionReportDTO> sessionDTOs = new ArrayList<>();

        if ("ACADEMIC".equalsIgnoreCase(mentorType)) {
            var sessions = academicSessionRepo.findByMentorIdAndSessionDateBetweenOrderBySessionDateDescSessionTimeDesc(
                    mentorId, startDate, endDate);
            for (var session : sessions) {
                sessionDTOs.add(buildSessionReportDTO(session.getSessionId(), session.getSessionTitle(),
                        session.getSessionTopic(), session.getSessionType() != null ? session.getSessionType().name() : null,
                        mentor.getFullName(), "Academic", session.getSessionDate(), session.getSessionTime(),
                        session.getAudienceMode(), session.getTargetStudentIds()));
            }
        } else {
            var sessions = industrySessionRepo.findByMentorIdAndSessionDateBetweenOrderBySessionDateDescSessionTimeDesc(
                    mentorId, startDate, endDate);
            for (var session : sessions) {
                sessionDTOs.add(buildSessionReportDTO(session.getSessionId(), session.getSessionTitle(),
                        session.getSessionTopic(), session.getSessionType() != null ? session.getSessionType().name() : null,
                        mentor.getFullName(), "Industry", session.getSessionDate(), session.getSessionTime(),
                        session.getAudienceMode(), session.getTargetStudentIds()));
            }
        }

        int totalParticipants = sessionDTOs.stream().mapToInt(SessionReportDTO::getParticipantCount).sum();

        SessionReportResponseDTO response = new SessionReportResponseDTO();
        response.setReportTitle("Mentor Session Report");
        response.setGeneratedBy(mentor.getFullName());
        response.setDepartment(mentor.getDepartment());
        response.setDateRange(startDate.toString() + " to " + endDate.toString());
        response.setGeneratedAt(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        response.setTotalSessions(sessionDTOs.size());
        response.setTotalParticipants(totalParticipants);
        response.setSessions(sessionDTOs);

        return response;
    }

    private SessionReportDTO buildSessionReportDTO(Integer sessionId, String title, String topic,
                                                     String sessionType, String mentorName, String mentorType,
                                                     java.time.LocalDate sessionDate, java.time.LocalTime sessionTime,
                                                     String audienceMode, String targetStudentIds) {
        List<StudentParticipantDTO> participants = resolveParticipants(targetStudentIds);
        SessionReportDTO dto = new SessionReportDTO();
        dto.setSessionId(sessionId);
        dto.setSessionTitle(title);
        dto.setSessionTopic(topic);
        dto.setSessionType(sessionType);
        dto.setMentorName(mentorName);
        dto.setMentorType(mentorType);
        dto.setSessionDate(sessionDate != null ? sessionDate.toString() : null);
        dto.setSessionTime(sessionTime != null ? sessionTime.toString() : null);
        dto.setAudienceMode(audienceMode);
        dto.setParticipantCount(participants.size());
        dto.setParticipants(participants);
        return dto;
    }

    private List<StudentParticipantDTO> resolveParticipants(String targetStudentIds) {
        if (targetStudentIds == null || targetStudentIds.isBlank()) {
            return Collections.emptyList();
        }
        return Arrays.stream(targetStudentIds.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(idStr -> {
                    try {
                        Long id = Long.parseLong(idStr);
                        return userRepository.findById(id)
                                .map(user -> new StudentParticipantDTO(
                                        user.getId(),
                                        user.getFullName(),
                                        user.getRegistrationNumber(),
                                        user.getCpmNumber(),
                                        user.getYearOfStudy(),
                                        user.getDepartment()))
                                .orElse(null);
                    } catch (NumberFormatException e) {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    // --- Existing methods (kept as-is) ---

    @Override
    public ReportResponseDTO generateReport(ReportRequestDTO requestDTO) {
        LocalDate startDate = LocalDate.parse(requestDTO.getStartDate());
        LocalDate endDate = LocalDate.parse(requestDTO.getEndDate());

        if (!endDate.isAfter(startDate)) {
            throw new InvalidDateRangeException(
                    "End date must be later than start date. Start: " + startDate + ", End: " + endDate);
        }

        ReportType reportType = ReportType.valueOf(requestDTO.getReportType());
        ReportFormat reportFormat = ReportFormat.valueOf(requestDTO.getReportFormat());

        String filePath = generateReportFile(requestDTO.getUserId(), reportType, reportFormat,
                startDate, endDate, requestDTO.getDepartment());

        Report report = new Report();
        report.setGeneratedBy(requestDTO.getUserId());
        report.setReportType(reportType);
        report.setStartDate(startDate);
        report.setEndDate(endDate);
        report.setDepartment(requestDTO.getDepartment() != null ? requestDTO.getDepartment() : "All Departments");
        report.setReportFormat(reportFormat);
        report.setFilePath(filePath);

        Report savedReport = reportRepository.save(report);
        return mapToResponseDTO(savedReport);
    }

    @Override
    public List<ReportResponseDTO> getReportsByUser(Integer userId) {
        List<Report> reports = reportRepository.findByGeneratedByOrderByGeneratedDateDesc(userId);
        return reports.stream().map(this::mapToResponseDTO).collect(Collectors.toList());
    }

    @Override
    public ReportResponseDTO getReportById(Integer reportId) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new ResourceNotFoundException("Report not found with ID: " + reportId));
        return mapToResponseDTO(report);
    }

    @Override
    public List<ReportResponseDTO> getAllReports() {
        List<Report> reports = reportRepository.findAll();
        return reports.stream().map(this::mapToResponseDTO).collect(Collectors.toList());
    }

    private String generateReportFile(Integer userId, ReportType reportType, ReportFormat reportFormat,
                                       LocalDate startDate, LocalDate endDate, String department) {
        File directory = new File(reportsDir);
        if (!directory.exists()) {
            directory.mkdirs();
        }

        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String extension = reportFormat == ReportFormat.PDF ? ".pdf" : ".xlsx";
        String fileName = reportType.name().toLowerCase() + "_" + timestamp + "_user" + userId + extension;
        String filePath = reportsDir + File.separator + fileName;

        try (FileWriter writer = new FileWriter(filePath)) {
            writer.write("Report Type: " + reportType.getDisplayName() + "\n");
            writer.write("Generated By: User " + userId + "\n");
            writer.write("Date Range: " + startDate + " to " + endDate + "\n");
            writer.write("Department: " + (department != null ? department : "All Departments") + "\n");
            writer.write("Generated On: " + LocalDateTime.now() + "\n");
            writer.write("Format: " + reportFormat.getDisplayName() + "\n");
        } catch (IOException e) {
            throw new ReportGenerationException("Failed to generate report file: " + e.getMessage());
        }

        return filePath;
    }

    private ReportResponseDTO mapToResponseDTO(Report report) {
        return ReportResponseDTO.builder()
                .reportId(report.getReportId())
                .generatedBy(report.getGeneratedBy())
                .reportType(report.getReportType().getDisplayName())
                .startDate(report.getStartDate())
                .endDate(report.getEndDate())
                .department(report.getDepartment())
                .reportFormat(report.getReportFormat().getDisplayName())
                .filePath(report.getFilePath())
                .generatedDate(report.getGeneratedDate())
                .build();
    }
}
