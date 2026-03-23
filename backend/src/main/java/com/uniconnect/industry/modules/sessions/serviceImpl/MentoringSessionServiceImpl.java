package com.uniconnect.industry.modules.sessions.serviceImpl;

import com.uniconnect.industry.common.exception.DuplicateSessionException;
import com.uniconnect.industry.modules.sessions.dto.SessionRequestDTO;
import com.uniconnect.industry.modules.sessions.dto.SessionResponseDTO;
import com.uniconnect.industry.modules.sessions.entity.MentoringSession;
import com.uniconnect.industry.modules.sessions.enums.SessionType;
import com.uniconnect.industry.modules.sessions.repository.MentoringSessionRepository;
import com.uniconnect.industry.modules.sessions.service.MentoringSessionService;
import com.uniconnect.model.User;
import com.uniconnect.repository.UserRepository;
import com.uniconnect.student.modules.feedback.entity.MentorSession;
import com.uniconnect.student.modules.feedback.enums.SessionStatus;
import com.uniconnect.student.modules.feedback.repository.MentorSessionRepository;
import com.uniconnect.student.modules.mentor.enums.ConnectionStatus;
import com.uniconnect.student.modules.mentor.enums.MentorType;
import com.uniconnect.student.modules.mentor.repository.MentorConnectionRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service("industryMentoringSessionService")
public class MentoringSessionServiceImpl implements MentoringSessionService {

    private final MentoringSessionRepository mentoringSessionRepository;
    private final MentorConnectionRepository mentorConnectionRepository;
    private final MentorSessionRepository mentorSessionRepository;
    private final UserRepository userRepository;

    public MentoringSessionServiceImpl(
            MentoringSessionRepository mentoringSessionRepository,
            MentorConnectionRepository mentorConnectionRepository,
            MentorSessionRepository mentorSessionRepository,
            UserRepository userRepository
    ) {
        this.mentoringSessionRepository = mentoringSessionRepository;
        this.mentorConnectionRepository = mentorConnectionRepository;
        this.mentorSessionRepository = mentorSessionRepository;
        this.userRepository = userRepository;
    }

    @Override
    public SessionResponseDTO createSession(SessionRequestDTO requestDTO) {
        LocalDateTime scheduledAt = LocalDateTime.of(requestDTO.getSessionDate(), requestDTO.getSessionTime());
        if (!scheduledAt.isAfter(LocalDateTime.now())) {
            throw new IllegalArgumentException("Session date and time must be in the future.");
        }

        if (mentoringSessionRepository.existsByMentorIdAndSessionDateAndSessionTime(
                requestDTO.getMentorId(), requestDTO.getSessionDate(), requestDTO.getSessionTime())) {
            throw new DuplicateSessionException(
                    "A session already exists at " + requestDTO.getSessionDate() +
                            " " + requestDTO.getSessionTime() + " for this mentor.");
        }

        SessionType sessionType;
        try {
            sessionType = SessionType.valueOf(requestDTO.getSessionType().toUpperCase().replace("-", "_"));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid session type: " + requestDTO.getSessionType() +
                    ". Must be ONE_TO_ONE or GROUP.");
        }

        List<com.uniconnect.student.modules.mentor.entity.MentorConnection> connections =
                mentorConnectionRepository.findByMentorIdAndMentorTypeAndConnectionStatus(
                        requestDTO.getMentorId(), MentorType.Industry, ConnectionStatus.Approved
                );
        Set<Integer> availableStudentIds = connections.stream()
                .map(com.uniconnect.student.modules.mentor.entity.MentorConnection::getStudentId)
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        Targeting targeting = resolveTargeting(requestDTO, sessionType, availableStudentIds);
        String sessionTopic = requestDTO.getSessionTopic() == null || requestDTO.getSessionTopic().trim().isEmpty()
                ? requestDTO.getSessionTitle().trim()
                : requestDTO.getSessionTopic().trim();

        MentoringSession session = new MentoringSession();
        session.setMentorId(requestDTO.getMentorId());
        session.setSessionTitle(requestDTO.getSessionTitle().trim());
        session.setSessionType(sessionType);
        session.setSessionTopic(sessionTopic);
        session.setSessionDescription(requestDTO.getSessionDescription());
        session.setAudienceMode(targeting.audienceMode());
        session.setTargetYearOfStudy(targeting.targetYearOfStudy());
        session.setTargetStudentIds(targeting.targetStudentIdsCsv());
        session.setSessionDate(requestDTO.getSessionDate());
        session.setSessionTime(requestDTO.getSessionTime());

        MentoringSession saved = mentoringSessionRepository.save(session);

        // Provision per-student feedback sessions for this industry mentor.
        provisionMentorSessions(saved, targeting.targetStudentIds());

        return mapToResponseDTO(saved);
    }

    private Targeting resolveTargeting(
            SessionRequestDTO requestDTO,
            SessionType sessionType,
            Set<Integer> availableStudentIds
    ) {
        List<Integer> requestedStudentIds = requestDTO.getTargetStudentIds() == null
                ? List.of()
                : requestDTO.getTargetStudentIds().stream()
                        .filter(java.util.Objects::nonNull)
                        .distinct()
                        .toList();

        if (sessionType == SessionType.ONE_TO_ONE) {
            if (requestedStudentIds.size() != 1) {
                throw new IllegalArgumentException("One-to-one sessions must target exactly one student.");
            }
            Integer studentId = requestedStudentIds.get(0);
            if (!availableStudentIds.contains(studentId)) {
                throw new IllegalArgumentException("Selected student is not connected to this mentor.");
            }
            return new Targeting("ONE_TO_ONE", null, studentIdsToCsv(List.of(studentId)), List.of(studentId));
        }

        String audienceMode = requestDTO.getAudienceMode() == null || requestDTO.getAudienceMode().trim().isEmpty()
                ? "ALL_ASSIGNED"
                : requestDTO.getAudienceMode().trim().toUpperCase();

        return switch (audienceMode) {
            case "ALL_ASSIGNED" -> new Targeting(
                    "ALL_ASSIGNED",
                    null,
                    studentIdsToCsv(availableStudentIds.stream().toList()),
                    availableStudentIds.stream().toList()
            );
            case "YEAR" -> {
                String year = requestDTO.getTargetYearOfStudy();
                if (year == null || year.trim().isEmpty()) {
                    throw new IllegalArgumentException("Group sessions by year require a year selection.");
                }
                List<Integer> yearStudentIds = connectionsForYear(availableStudentIds, year.trim());
                if (yearStudentIds.isEmpty()) {
                    throw new IllegalArgumentException("No connected students found for the selected year.");
                }
                yield new Targeting("YEAR", year.trim(), studentIdsToCsv(yearStudentIds), yearStudentIds);
            }
            case "CUSTOM" -> {
                if (requestedStudentIds.isEmpty()) {
                    throw new IllegalArgumentException("Custom group sessions require at least one student.");
                }
                boolean invalidStudent = requestedStudentIds.stream().anyMatch(id -> !availableStudentIds.contains(id));
                if (invalidStudent) {
                    throw new IllegalArgumentException("Custom group contains students not connected to this mentor.");
                }
                yield new Targeting("CUSTOM", null, studentIdsToCsv(requestedStudentIds), requestedStudentIds);
            }
            default -> throw new IllegalArgumentException("Invalid audience mode: " + audienceMode);
        };
    }

    private List<Integer> connectionsForYear(Set<Integer> availableStudentIds, String year) {
        return availableStudentIds.stream()
                .filter(studentId -> {
                    User student = userRepository.findById(studentId.longValue()).orElse(null);
                    return student != null && year.equalsIgnoreCase(student.getYearOfStudy());
                })
                .toList();
    }

    private void provisionMentorSessions(MentoringSession savedSession, List<Integer> targetStudentIds) {
        Integer mentorId = savedSession.getMentorId();
        if (mentorId == null) return;

        LocalDateTime sessionDateTime = LocalDateTime.of(savedSession.getSessionDate(), savedSession.getSessionTime());
        SessionStatus status = sessionDateTime.isAfter(LocalDateTime.now()) ? SessionStatus.Scheduled : SessionStatus.Completed;

        List<com.uniconnect.student.modules.mentor.entity.MentorConnection> connections =
                mentorConnectionRepository.findByMentorIdAndMentorTypeAndConnectionStatus(
                        mentorId, MentorType.Industry, ConnectionStatus.Approved
                );

        for (com.uniconnect.student.modules.mentor.entity.MentorConnection connection : connections) {
            if (targetStudentIds != null && !targetStudentIds.contains(connection.getStudentId())) continue;
            MentorSession mentorSession = new MentorSession();
            mentorSession.setConnectionId(connection.getConnectionId());
            mentorSession.setStudentId(connection.getStudentId());
            mentorSession.setMentorId(mentorId);
            mentorSession.setSessionStatus(status);
            mentorSession.setSessionDate(sessionDateTime);
            mentorSessionRepository.save(mentorSession);
        }
    }

    @Override
    public List<SessionResponseDTO> getSessionsByMentor(Integer mentorId) {
        return mentoringSessionRepository.findByMentorIdOrderBySessionDateDescSessionTimeDesc(mentorId)
                .stream().map(this::mapToResponseDTO).collect(Collectors.toList());
    }

    private SessionResponseDTO mapToResponseDTO(MentoringSession session) {
        return SessionResponseDTO.builder()
                .sessionId(session.getSessionId())
                .mentorId(session.getMentorId())
                .sessionTitle(session.getSessionTitle())
                .sessionType(session.getSessionType().name())
                .sessionTopic(session.getSessionTopic())
                .sessionDescription(session.getSessionDescription())
                .audienceMode(session.getAudienceMode())
                .targetYearOfStudy(session.getTargetYearOfStudy())
                .targetStudentIds(session.getTargetStudentIds())
                .sessionDate(session.getSessionDate())
                .sessionTime(session.getSessionTime())
                .createdDate(session.getCreatedDate())
                .build();
    }

    private String studentIdsToCsv(List<Integer> studentIds) {
        return studentIds.stream().map(String::valueOf).collect(Collectors.joining(","));
    }

    private record Targeting(String audienceMode, String targetYearOfStudy, String targetStudentIdsCsv, List<Integer> targetStudentIds) {
    }
}
