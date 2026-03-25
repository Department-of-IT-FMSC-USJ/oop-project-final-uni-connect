package com.uniconnect.student.modules.feedback.service;

import com.uniconnect.student.modules.feedback.dto.MentorFeedbackRequestDTO;
import com.uniconnect.student.modules.feedback.dto.MentorFeedbackResponseDTO;
import com.uniconnect.student.modules.feedback.dto.PendingFeedbackSessionDTO;

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

    /**
     * Return completed mentor sessions that do not yet have feedback from the student.
     */
    List<PendingFeedbackSessionDTO> getPendingCompletedSessions(Integer studentId);
}
