package com.uniconnect.industry.modules.sessions.service;

import com.uniconnect.industry.modules.sessions.dto.SessionRequestDTO;
import com.uniconnect.industry.modules.sessions.dto.SessionResponseDTO;

import java.util.List;

public interface MentoringSessionService {

    SessionResponseDTO createSession(SessionRequestDTO requestDTO);

    List<SessionResponseDTO> getSessionsByMentor(Integer mentorId);

    void cancelSession(Integer sessionId, Integer mentorId);
}
