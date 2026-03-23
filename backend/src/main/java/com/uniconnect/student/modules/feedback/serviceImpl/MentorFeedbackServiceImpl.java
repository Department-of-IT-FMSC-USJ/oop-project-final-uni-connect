package com.uniconnect.student.modules.feedback.serviceImpl;

import com.uniconnect.student.modules.feedback.dto.MentorFeedbackRequestDTO;
import com.uniconnect.student.modules.feedback.dto.MentorFeedbackResponseDTO;
import com.uniconnect.student.modules.feedback.dto.PendingFeedbackSessionDTO;
import com.uniconnect.student.modules.feedback.entity.MentorFeedback;
import com.uniconnect.student.modules.feedback.entity.MentorSession;
import com.uniconnect.student.modules.feedback.enums.SessionStatus;
import com.uniconnect.student.modules.feedback.exception.DuplicateFeedbackException;
import com.uniconnect.student.common.exception.ResourceNotFoundException;
import com.uniconnect.student.modules.feedback.exception.SessionNotCompletedException;
import com.uniconnect.repository.UserRepository;
import com.uniconnect.student.modules.feedback.repository.MentorFeedbackRepository;
import com.uniconnect.student.modules.feedback.repository.MentorSessionRepository;
import com.uniconnect.student.modules.feedback.service.MentorFeedbackService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of MentorFeedbackService.
 * Handles business logic for feedback storage and review operations.
 */
@Service
public class MentorFeedbackServiceImpl implements MentorFeedbackService {

    private final MentorFeedbackRepository mentorFeedbackRepository;
    private final MentorSessionRepository mentorSessionRepository;
    private final UserRepository userRepository;

    public MentorFeedbackServiceImpl(MentorFeedbackRepository mentorFeedbackRepository,
                                      MentorSessionRepository mentorSessionRepository,
                                      UserRepository userRepository) {
        this.mentorFeedbackRepository = mentorFeedbackRepository;
        this.mentorSessionRepository = mentorSessionRepository;
        this.userRepository = userRepository;
    }

    /**
     * Submit feedback for a completed mentor session.
     * Validates that the session exists, is completed, and no duplicate feedback exists.
     */
    @Override
    public MentorFeedbackResponseDTO submitFeedback(MentorFeedbackRequestDTO requestDTO) {

        // Find the session and validate it exists
        MentorSession session = mentorSessionRepository.findById(requestDTO.getSessionId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Mentor session not found with ID: " + requestDTO.getSessionId()));

        // Session is considered eligible when either:
        // - status is explicitly completed, OR
        // - sessionDate is in the past (<= now).
        if (session.getSessionStatus() != SessionStatus.Completed
                && session.getSessionDate() != null
                && session.getSessionDate().isAfter(LocalDateTime.now())) {
            throw new SessionNotCompletedException(
                    "Feedback can only be submitted for completed sessions. " +
                            "Current session status: " + session.getSessionStatus());
        }

        // Prevent duplicate feedback per session by the same student
        if (mentorFeedbackRepository.existsBySessionIdAndStudentId(
                requestDTO.getSessionId(), requestDTO.getStudentId())) {
            throw new DuplicateFeedbackException(
                    "Feedback has already been submitted for session ID: " + requestDTO.getSessionId() +
                            " by student ID: " + requestDTO.getStudentId());
        }

        // Create and save the feedback
        MentorFeedback feedback = new MentorFeedback();
        feedback.setSessionId(requestDTO.getSessionId());
        feedback.setStudentId(requestDTO.getStudentId());
        feedback.setRating(requestDTO.getRating());
        feedback.setFeedbackComment(requestDTO.getFeedbackComment());

        MentorFeedback savedFeedback = mentorFeedbackRepository.save(feedback);

        return mapToResponseDTO(savedFeedback);
    }

    /**
     * Get all feedback for a specific session.
     */
    @Override
    public List<MentorFeedbackResponseDTO> getFeedbackBySession(Integer sessionId) {
        List<MentorFeedback> feedbackList =
                mentorFeedbackRepository.findBySessionIdOrderBySubmittedDateDesc(sessionId);

        return feedbackList.stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<PendingFeedbackSessionDTO> getPendingCompletedSessions(Integer studentId) {
        LocalDateTime now = LocalDateTime.now();

        List<MentorSession> sessions =
                mentorSessionRepository.findByStudentIdOrderBySessionDateDesc(studentId);

        return sessions.stream()
                .filter(s -> s.getSessionDate() != null && !s.getSessionDate().isAfter(now))
                .filter(s -> !mentorFeedbackRepository.existsBySessionIdAndStudentId(s.getSessionId(), studentId))
                .map(s -> {
                    String mentorName = userRepository.findById(s.getMentorId().longValue())
                            .map(u -> u.getFullName())
                            .orElse(null);

                    return new PendingFeedbackSessionDTO(
                            s.getSessionId(),
                            s.getMentorId(),
                            mentorName,
                            s.getSessionDate()
                    );
                })
                .collect(Collectors.toList());
    }

    /**
     * Map MentorFeedback entity to MentorFeedbackResponseDTO.
     *
     * @param feedback the entity to map
     * @return the response DTO
     */
    private MentorFeedbackResponseDTO mapToResponseDTO(MentorFeedback feedback) {
        return MentorFeedbackResponseDTO.builder()
                .feedbackId(feedback.getFeedbackId())
                .sessionId(feedback.getSessionId())
                .studentId(feedback.getStudentId())
                .rating(feedback.getRating())
                .feedbackComment(feedback.getFeedbackComment())
                .submittedDate(feedback.getSubmittedDate())
                .build();
    }
}
