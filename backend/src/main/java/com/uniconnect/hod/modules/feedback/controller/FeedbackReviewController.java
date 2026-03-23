package com.uniconnect.hod.modules.feedback.controller;

import com.uniconnect.hod.common.dto.ApiResponseDTO;
import com.uniconnect.hod.common.exception.UnauthorizedAccessException;
import com.uniconnect.hod.modules.feedback.dto.FeedbackReviewResponseDTO;
import com.uniconnect.model.Role;
import com.uniconnect.model.User;
import com.uniconnect.repository.UserRepository;
import com.uniconnect.student.modules.feedback.entity.MentorFeedback;
import com.uniconnect.student.modules.feedback.entity.MentorSession;
import com.uniconnect.student.modules.feedback.repository.MentorFeedbackRepository;
import com.uniconnect.student.modules.feedback.repository.MentorSessionRepository;
import jakarta.validation.constraints.Min;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@RestController
@RequestMapping("/api/feedback-reviews")
public class FeedbackReviewController {

    private final MentorFeedbackRepository mentorFeedbackRepository;
    private final MentorSessionRepository mentorSessionRepository;
    private final UserRepository userRepository;

    public FeedbackReviewController(MentorFeedbackRepository mentorFeedbackRepository,
                                     MentorSessionRepository mentorSessionRepository,
                                     UserRepository userRepository) {
        this.mentorFeedbackRepository = mentorFeedbackRepository;
        this.mentorSessionRepository = mentorSessionRepository;
        this.userRepository = userRepository;
    }

    /**
     * HoD feedback review dashboard.
     *
     * GET /api/feedback-reviews
     *
     * Query params:
     * - minRating (optional)
     * - maxRating (optional)
     * - upToDate=true/false (optional, default false)
     * - days (optional, default 30; used when upToDate=true)
     */
    @GetMapping
    public ResponseEntity<ApiResponseDTO<List<FeedbackReviewResponseDTO>>> getFeedbackReviews(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(value = "minRating", required = false) @Min(1) Integer minRating,
            @RequestParam(value = "maxRating", required = false) @Min(1) Integer maxRating,
            @RequestParam(value = "upToDate", required = false, defaultValue = "false") boolean upToDate,
            @RequestParam(value = "days", required = false, defaultValue = "30") @Min(1) Integer days
    ) {
        if (userDetails == null) {
            throw new UnauthorizedAccessException("Authentication required.");
        }

        User hodUser = userRepository.findByEmail(userDetails.getUsername())
                .orElse(null);
        if (hodUser == null || hodUser.getRole() == null
                || (hodUser.getRole() != Role.DEPARTMENT_HEAD && hodUser.getRole() != Role.DEPARTMENT_ASSISTANT)) {
            throw new UnauthorizedAccessException("Only HoD workspace users can access feedback reviews.");
        }

        LocalDateTime cutoff = null;
        if (upToDate) {
            cutoff = LocalDateTime.now().minusDays(days);
        }

        List<MentorFeedback> allFeedback = mentorFeedbackRepository.findAll();
        List<FeedbackReviewResponseDTO> result = new ArrayList<>();

        for (MentorFeedback feedback : allFeedback) {
            if (minRating != null && feedback.getRating() < minRating) continue;
            if (maxRating != null && feedback.getRating() > maxRating) continue;
            if (cutoff != null && (feedback.getSubmittedDate() == null || feedback.getSubmittedDate().isBefore(cutoff)))
                continue;

            MentorSession session = mentorSessionRepository.findById(feedback.getSessionId()).orElse(null);
            if (session == null || session.getMentorId() == null) continue;

            Integer mentorId = session.getMentorId();
            String mentorName = userRepository.findById(mentorId.longValue())
                    .map(User::getFullName)
                    .orElse("Unknown");

            result.add(new FeedbackReviewResponseDTO(
                    feedback.getSessionId(),
                    mentorName,
                    feedback.getRating(),
                    feedback.getFeedbackComment(),
                    feedback.getSubmittedDate()
            ));
        }

        result.sort(
                Comparator.comparing(FeedbackReviewResponseDTO::getRating)
                        .thenComparing(FeedbackReviewResponseDTO::getSubmittedDate,
                                Comparator.nullsLast(Comparator.reverseOrder()))
        );

        ApiResponseDTO<List<FeedbackReviewResponseDTO>> response = ApiResponseDTO.success(
                "Feedback reviews retrieved successfully", result);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
