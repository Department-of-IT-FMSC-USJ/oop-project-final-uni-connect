package com.uniconnect.industry.modules.sessions.serviceImpl;

import com.uniconnect.industry.common.exception.DuplicateSessionException;
import com.uniconnect.industry.modules.sessions.dto.SessionRequestDTO;
import com.uniconnect.industry.modules.sessions.dto.SessionResponseDTO;
import com.uniconnect.industry.modules.sessions.entity.MentoringSession;
import com.uniconnect.industry.modules.sessions.enums.SessionType;
import com.uniconnect.industry.modules.sessions.repository.MentoringSessionRepository;
import com.uniconnect.industry.modules.sessions.service.MentoringSessionService;
import com.uniconnect.student.modules.feedback.entity.MentorSession;
import com.uniconnect.student.modules.feedback.enums.SessionStatus;
import com.uniconnect.student.modules.feedback.repository.MentorSessionRepository;
import com.uniconnect.student.modules.mentor.enums.ConnectionStatus;
import com.uniconnect.student.modules.mentor.enums.MentorType;
import com.uniconnect.student.modules.mentor.repository.MentorConnectionRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service("industryMentoringSessionService")
public class MentoringSessionServiceImpl implements MentoringSessionService {

    private final MentoringSessionRepository mentoringSessionRepository;
    private final MentorConnectionRepository mentorConnectionRepository;
    private final MentorSessionRepository mentorSessionRepository;

    public MentoringSessionServiceImpl(
            MentoringSessionRepository mentoringSessionRepository,
            MentorConnectionRepository mentorConnectionRepository,
            MentorSessionRepository mentorSessionRepository
    ) {
        this.mentoringSessionRepository = mentoringSessionRepository;
        this.mentorConnectionRepository = mentorConnectionRepository;
        this.mentorSessionRepository = mentorSessionRepository;
    }

    @Override
    public SessionResponseDTO createSession(SessionRequestDTO requestDTO) {
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

        MentoringSession session = new MentoringSession();
        session.setMentorId(requestDTO.getMentorId());
        session.setSessionTitle(requestDTO.getSessionTitle());
        session.setSessionType(sessionType);
        session.setSessionTopic(requestDTO.getSessionTopic());
        session.setSessionDescription(requestDTO.getSessionDescription());
        session.setSessionDate(requestDTO.getSessionDate());
        session.setSessionTime(requestDTO.getSessionTime());

        MentoringSession saved = mentoringSessionRepository.save(session);

        // Provision per-student feedback sessions for this industry mentor.
        provisionMentorSessions(saved);

        return mapToResponseDTO(saved);
    }

    private void provisionMentorSessions(MentoringSession savedSession) {
        Integer mentorId = savedSession.getMentorId();
        if (mentorId == null) return;

        LocalDateTime sessionDateTime = LocalDateTime.of(savedSession.getSessionDate(), savedSession.getSessionTime());
        SessionStatus status = sessionDateTime.isAfter(LocalDateTime.now()) ? SessionStatus.Scheduled : SessionStatus.Completed;

        List<com.uniconnect.student.modules.mentor.entity.MentorConnection> connections =
                mentorConnectionRepository.findByMentorIdAndMentorTypeAndConnectionStatus(
                        mentorId, MentorType.Industry, ConnectionStatus.Approved
                );

        for (com.uniconnect.student.modules.mentor.entity.MentorConnection connection : connections) {
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
                .sessionDate(session.getSessionDate())
                .sessionTime(session.getSessionTime())
                .createdDate(session.getCreatedDate())
                .build();
    }
}
