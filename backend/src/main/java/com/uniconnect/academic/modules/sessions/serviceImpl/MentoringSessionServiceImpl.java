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
import com.uniconnect.student.modules.feedback.entity.MentorSession;
import com.uniconnect.student.modules.feedback.enums.SessionStatus;
import com.uniconnect.student.modules.feedback.repository.MentorSessionRepository;
import com.uniconnect.student.modules.mentor.enums.ConnectionStatus;
import com.uniconnect.student.modules.mentor.enums.MentorType;
import com.uniconnect.student.modules.mentor.repository.MentorConnectionRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.time.LocalDateTime;
import java.util.Optional;
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

    public MentoringSessionServiceImpl(
            MentoringSessionRepository mentoringSessionRepository,
            StudentAllocationRepository studentAllocationRepository,
            MentorConnectionRepository mentorConnectionRepository,
            MentorSessionRepository mentorSessionRepository
    ) {
        this.mentoringSessionRepository = mentoringSessionRepository;
        this.studentAllocationRepository = studentAllocationRepository;
        this.mentorConnectionRepository = mentorConnectionRepository;
        this.mentorSessionRepository = mentorSessionRepository;
    }

    /**
     * Create a new mentoring session.
     * Validates that no duplicate session exists at the same date and time for the mentor.
     */
    @Override
    public SessionResponseDTO createSession(SessionRequestDTO requestDTO) {

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

        // Create and save the session
        MentoringSession session = new MentoringSession();
        session.setMentorId(requestDTO.getMentorId());
        session.setSessionTitle(requestDTO.getSessionTitle());
        session.setSessionType(sessionType);
        session.setSessionTopic(requestDTO.getSessionTopic());
        session.setSessionDescription(requestDTO.getSessionDescription());
        session.setSessionDate(requestDTO.getSessionDate());
        session.setSessionTime(requestDTO.getSessionTime());

        MentoringSession savedSession = mentoringSessionRepository.save(session);

        // Provision per-student feedback sessions for this academic mentor.
        provisionMentorSessions(savedSession);

        return mapToResponseDTO(savedSession);
    }

    private void provisionMentorSessions(MentoringSession savedSession) {
        Integer mentorId = savedSession.getMentorId();
        if (mentorId == null) return;

        LocalDateTime sessionDateTime = LocalDateTime.of(savedSession.getSessionDate(), savedSession.getSessionTime());
        SessionStatus status = sessionDateTime.isAfter(LocalDateTime.now()) ? SessionStatus.Scheduled : SessionStatus.Completed;

        List<StudentAllocation> allocations =
                studentAllocationRepository.findByMentorIdOrderByAllocationDateDesc(mentorId);

        for (StudentAllocation allocation : allocations) {
            Integer studentId = allocation.getStudentId();
            if (studentId == null) continue;

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
                .sessionDate(session.getSessionDate())
                .sessionTime(session.getSessionTime())
                .createdAt(session.getCreatedAt())
                .build();
    }
}
