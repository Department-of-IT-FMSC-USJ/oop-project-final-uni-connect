package com.uniconnect.academic.modules.sessions.service;

import com.uniconnect.academic.modules.sessions.dto.SessionRequestDTO;
import com.uniconnect.academic.modules.sessions.dto.SessionResponseDTO;

import java.util.List;

/**
 * Service interface for Mentoring Session operations.
 */
public interface MentoringSessionService {

    /**
     * Create a new mentoring session.
     *
     * @param requestDTO the session details
     * @return the created session details
     */
    SessionResponseDTO createSession(SessionRequestDTO requestDTO);

    /**
     * Get all sessions for a specific mentor.
     *
     * @param mentorId the mentor ID
     * @return list of session records
     */
    List<SessionResponseDTO> getSessionsByMentor(Integer mentorId);
}
