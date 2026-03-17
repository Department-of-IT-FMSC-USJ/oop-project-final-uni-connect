package com.industrymentor.modules.sessions.serviceImpl;

import com.industrymentor.common.exception.DuplicateSessionException;
import com.industrymentor.modules.sessions.dto.SessionRequestDTO;
import com.industrymentor.modules.sessions.dto.SessionResponseDTO;
import com.industrymentor.modules.sessions.entity.MentoringSession;
import com.industrymentor.modules.sessions.enums.SessionType;
import com.industrymentor.modules.sessions.repository.MentoringSessionRepository;
import com.industrymentor.modules.sessions.service.MentoringSessionService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class MentoringSessionServiceImpl implements MentoringSessionService {

    private final MentoringSessionRepository mentoringSessionRepository;

    public MentoringSessionServiceImpl(MentoringSessionRepository mentoringSessionRepository) {
        this.mentoringSessionRepository = mentoringSessionRepository;
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
        return mapToResponseDTO(saved);
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
