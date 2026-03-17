package com.hodportal.modules.feedbackreview.serviceImpl;

import com.hodportal.modules.feedbackreview.dto.FeedbackResponseDTO;
import com.hodportal.modules.feedbackreview.entity.FeedbackReview;
import com.hodportal.modules.feedbackreview.repository.FeedbackReviewRepository;
import com.hodportal.modules.feedbackreview.service.FeedbackReviewService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of FeedbackReviewService.
 * Handles business logic for feedback review retrieval.
 */
@Service
public class FeedbackReviewServiceImpl implements FeedbackReviewService {

    private final FeedbackReviewRepository feedbackReviewRepository;

    public FeedbackReviewServiceImpl(FeedbackReviewRepository feedbackReviewRepository) {
        this.feedbackReviewRepository = feedbackReviewRepository;
    }

    /**
     * Get all feedback review records.
     */
    @Override
    public List<FeedbackResponseDTO> getAllFeedbackReviews() {
        List<FeedbackReview> feedbacks = feedbackReviewRepository.findAllByOrderBySubmittedDateDesc();

        return feedbacks.stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Map FeedbackReview entity to FeedbackResponseDTO.
     */
    private FeedbackResponseDTO mapToResponseDTO(FeedbackReview feedback) {
        return FeedbackResponseDTO.builder()
                .feedbackId(feedback.getFeedbackId())
                .sessionId(feedback.getSessionId())
                .studentId(feedback.getStudentId())
                .mentorName(feedback.getMentorName())
                .rating(feedback.getRating())
                .feedbackComment(feedback.getFeedbackComment())
                .submittedDate(feedback.getSubmittedDate())
                .build();
    }
}
