package com.uniconnect.academic.modules.sessions.serviceImpl;

import com.uniconnect.academic.modules.sessions.dto.SessionRequestDTO;
import com.uniconnect.academic.modules.sessions.dto.SessionResponseDTO;
import com.uniconnect.academic.modules.sessions.entity.MentoringSession;
import com.uniconnect.academic.modules.sessions.enums.SessionType;
import com.uniconnect.academic.modules.sessions.exception.DuplicateSessionException;
import com.uniconnect.academic.modules.sessions.repository.MentoringSessionRepository;
import com.uniconnect.academic.modules.sessions.service.MentoringSessionService;
import com.uniconnect.academic.modules.allocations.repository.StudentAllocationRepository;
import com.uniconnect.academic.modules.allocations.entity.StudentAllocation;
import com.uniconnect.model.User;
import com.uniconnect.repository.UserRepository;
import com.uniconnect.student.modules.feedback.entity.MentorSession;
import com.uniconnect.student.modules.feedback.enums.SessionStatus;
import com.uniconnect.student.modules.feedback.repository.MentorSessionRepository;
import com.uniconnect.student.modules.mentor.enums.ConnectionStatus;
import com.uniconnect.student.modules.mentor.enums.MentorType;
import com.uniconnect.student.modules.mentor.repository.MentorConnectionRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Implementation of MentoringSessionService.
 * Handles business logic for session creation and retrieval.
 */
@Service("academicMentoringSessionService")
public class MentoringSessionServiceImpl implements MentoringSessionService {

    private final MentoringSessionRepository mentoringSessionRepository;
    private final StudentAllocationRepository studentAllocationRepository;
    private final MentorConnectionRepository mentorConnectionRepository;
    private final MentorSessionRepository mentorSessionRepository;
    private final UserRepository userRepository;

    public MentoringSessionServiceImpl(
            MentoringSessionRepository mentoringSessionRepository,
            StudentAllocationRepository studentAllocationRepository,
            MentorConnectionRepository mentorConnectionRepository,
            MentorSessionRepository mentorSessionRepository,
            UserRepository userRepository
    ) {
        this.mentoringSessionRepository = mentoringSessionRepository;
        this.studentAllocationRepository = studentAllocationRepository;
        this.mentorConnectionRepository = mentorConnectionRepository;
        this.mentorSessionRepository = mentorSessionRepository;
        this.userRepository = userRepository;
    }

    /**
     * Create a new mentoring session.
     * Validates that no duplicate session exists at the same date and time for the mentor.
     */
    @Override
    public SessionResponseDTO createSession(SessionRequestDTO requestDTO) {
        LocalDateTime scheduledAt = LocalDateTime.of(requestDTO.getSessionDate(), requestDTO.getSessionTime());
        if (!scheduledAt.isAfter(LocalDateTime.now())) {
            throw new IllegalArgumentException("Session date and time must be in the future.");
        }

        // Prevent duplicate sessions at the same date and time
        if (mentoringSessionRepository.existsByMentorIdAndSessionDateAndSessionTime(
                requestDTO.getMentorId(), requestDTO.getSessionDate(), requestDTO.getSessionTime())) {
            throw new DuplicateSessionException(
                    "A session already exists at " + requestDTO.getSessionDate() +
                            " " + requestDTO.getSessionTime() + " for this mentor.");
        }

        // Parse session type
        SessionType sessionType;
        try {
            sessionType = SessionType.valueOf(requestDTO.getSessionType().toUpperCase().replace("-", "_"));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid session type: " + requestDTO.getSessionType() +
                    ". Must be ONE_TO_ONE or GROUP.");
        }

        List<StudentAllocation> allocations =
                studentAllocationRepository.findByMentorIdOrderByAllocationDateDesc(requestDTO.getMentorId());
        Set<Integer> availableStudentIds = allocations.stream()
                .map(StudentAllocation::getStudentId)
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        Targeting targeting = resolveTargeting(requestDTO, sessionType, allocations, availableStudentIds);
        String sessionTopic = requestDTO.getSessionTopic() == null || requestDTO.getSessionTopic().trim().isEmpty()
                ? requestDTO.getSessionTitle().trim()
                : requestDTO.getSessionTopic().trim();

        // Create and save the session
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

        MentoringSession savedSession = mentoringSessionRepository.save(session);

        // Provision per-student feedback sessions for this academic mentor.
        provisionMentorSessions(savedSession, targeting.targetStudentIds());

        return mapToResponseDTO(savedSession);
    }

    private Targeting resolveTargeting(
            SessionRequestDTO requestDTO,
            SessionType sessionType,
            List<StudentAllocation> allocations,
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
                throw new IllegalArgumentException("Selected student is not assigned to this mentor.");
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
                List<Integer> yearStudentIds = allocations.stream()
                        .filter(allocation -> allocation.getStudentId() != null)
                        .filter(allocation -> {
                            User student = userRepository.findById(allocation.getStudentId().longValue()).orElse(null);
                            return student != null && year.trim().equalsIgnoreCase(student.getYearOfStudy());
                        })
                        .map(StudentAllocation::getStudentId)
                        .distinct()
                        .toList();
                if (yearStudentIds.isEmpty()) {
                    throw new IllegalArgumentException("No assigned students found for the selected year.");
                }
                yield new Targeting("YEAR", year.trim(), studentIdsToCsv(yearStudentIds), yearStudentIds);
            }
            case "CUSTOM" -> {
                if (requestedStudentIds.isEmpty()) {
                    throw new IllegalArgumentException("Custom group sessions require at least one student.");
                }
                boolean invalidStudent = requestedStudentIds.stream().anyMatch(id -> !availableStudentIds.contains(id));
                if (invalidStudent) {
                    throw new IllegalArgumentException("Custom group contains students not assigned to this mentor.");
                }
                yield new Targeting("CUSTOM", null, studentIdsToCsv(requestedStudentIds), requestedStudentIds);
            }
            default -> throw new IllegalArgumentException("Invalid audience mode: " + audienceMode);
        };
    }

    private void provisionMentorSessions(MentoringSession savedSession, List<Integer> targetStudentIds) {
        Integer mentorId = savedSession.getMentorId();
        if (mentorId == null) return;

        LocalDateTime sessionDateTime = LocalDateTime.of(savedSession.getSessionDate(), savedSession.getSessionTime());
        SessionStatus status = sessionDateTime.isAfter(LocalDateTime.now()) ? SessionStatus.Scheduled : SessionStatus.Completed;

        List<StudentAllocation> allocations =
                studentAllocationRepository.findByMentorIdOrderByAllocationDateDesc(mentorId);

        for (StudentAllocation allocation : allocations) {
            Integer studentId = allocation.getStudentId();
            if (studentId == null) continue;
            if (targetStudentIds != null && !targetStudentIds.contains(studentId)) continue;

            Optional<com.uniconnect.student.modules.mentor.entity.MentorConnection> connectionOpt =
                    mentorConnectionRepository.findByStudentIdAndMentorIdAndMentorTypeAndConnectionStatus(
                            studentId, mentorId, MentorType.Academic, ConnectionStatus.Approved);

            if (connectionOpt.isEmpty()) {
                // No approved connection means no feedback row should be created.
                continue;
            }

            MentorSession mentorSession = new MentorSession();
            mentorSession.setConnectionId(connectionOpt.get().getConnectionId());
            mentorSession.setStudentId(studentId);
            mentorSession.setMentorId(mentorId);
            mentorSession.setSessionStatus(status);
            mentorSession.setSessionDate(sessionDateTime);

            mentorSessionRepository.save(mentorSession);
        }
    }

    /**
     * Get all sessions for a specific mentor.
     */
    @Override
    public List<SessionResponseDTO> getSessionsByMentor(Integer mentorId) {
        List<MentoringSession> sessions =
                mentoringSessionRepository.findByMentorIdOrderBySessionDateDescSessionTimeDesc(mentorId);

        return sessions.stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Map MentoringSession entity to SessionResponseDTO.
     */
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
                .createdAt(session.getCreatedAt())
                .build();
    }

    private String studentIdsToCsv(List<Integer> studentIds) {
        return studentIds.stream().map(String::valueOf).collect(Collectors.joining(","));
    }

    private record Targeting(String audienceMode, String targetYearOfStudy, String targetStudentIdsCsv, List<Integer> targetStudentIds) {
    }
}
