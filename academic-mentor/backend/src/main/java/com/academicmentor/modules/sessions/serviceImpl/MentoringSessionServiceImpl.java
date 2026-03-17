package com.academicmentor.modules.sessions.serviceImpl;

import com.academicmentor.modules.sessions.dto.SessionRequestDTO;
import com.academicmentor.modules.sessions.dto.SessionResponseDTO;
import com.academicmentor.modules.sessions.entity.MentoringSession;
import com.academicmentor.modules.sessions.enums.SessionType;
import com.academicmentor.modules.sessions.exception.DuplicateSessionException;
import com.academicmentor.modules.sessions.repository.MentoringSessionRepository;
import com.academicmentor.modules.sessions.service.MentoringSessionService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of MentoringSessionService.
 * Handles business logic for session creation and retrieval.
 */
@Service
public class MentoringSessionServiceImpl implements MentoringSessionService {

    private final MentoringSessionRepository mentoringSessionRepository;

    public MentoringSessionServiceImpl(MentoringSessionRepository mentoringSessionRepository) {
        this.mentoringSessionRepository = mentoringSessionRepository;
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

        return mapToResponseDTO(savedSession);
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
