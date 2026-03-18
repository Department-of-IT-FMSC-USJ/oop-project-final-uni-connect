package com.industrymentor.modules.sessions.service;

import com.industrymentor.modules.sessions.dto.SessionRequestDTO;
import com.industrymentor.modules.sessions.dto.SessionResponseDTO;

import java.util.List;

public interface MentoringSessionService {

    SessionResponseDTO createSession(SessionRequestDTO requestDTO);

    List<SessionResponseDTO> getSessionsByMentor(Integer mentorId);
}
