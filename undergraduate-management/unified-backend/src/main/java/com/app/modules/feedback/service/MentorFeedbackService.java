package com.app.modules.feedback.service;

import com.app.modules.feedback.dto.MentorFeedbackRequestDTO;
import com.app.modules.feedback.dto.MentorFeedbackResponseDTO;

import java.util.List;

/**
 * Service interface for Mentor Feedback operations.
 */
public interface MentorFeedbackService {

    /**
     * Submit feedback for a completed mentor session.
     *
     * @param requestDTO the feedback details
     * @return the submitted feedback details
     */
    MentorFeedbackResponseDTO submitFeedback(MentorFeedbackRequestDTO requestDTO);

    /**
     * Get all feedback for a specific session.
     *
     * @param sessionId the session ID
     * @return list of feedback records
     */
    List<MentorFeedbackResponseDTO> getFeedbackBySession(Integer sessionId);
}
