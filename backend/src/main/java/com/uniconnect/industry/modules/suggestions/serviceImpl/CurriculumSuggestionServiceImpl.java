package com.uniconnect.industry.modules.suggestions.serviceImpl;

import com.uniconnect.industry.common.exception.DuplicateSuggestionException;
import com.uniconnect.industry.modules.suggestions.dto.SuggestionRequestDTO;
import com.uniconnect.industry.modules.suggestions.dto.SuggestionResponseDTO;
import com.uniconnect.industry.modules.suggestions.entity.CurriculumSuggestion;
import com.uniconnect.industry.modules.suggestions.entity.CurriculumSuggestionWindow;
import com.uniconnect.industry.modules.suggestions.enums.ReviewStatus;
import com.uniconnect.industry.modules.suggestions.enums.SuggestionCategory;
import com.uniconnect.industry.modules.suggestions.repository.CurriculumSuggestionRepository;
import com.uniconnect.industry.modules.suggestions.repository.CurriculumSuggestionWindowRepository;
import com.uniconnect.industry.modules.suggestions.service.CurriculumSuggestionService;
import com.uniconnect.model.Role;
import com.uniconnect.model.User;
import com.uniconnect.repository.UserRepository;
import com.uniconnect.service.SystemNotificationService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
public class CurriculumSuggestionServiceImpl implements CurriculumSuggestionService {

    private final CurriculumSuggestionRepository suggestionRepository;
    private final CurriculumSuggestionWindowRepository suggestionWindowRepository;
    private final UserRepository userRepository;
    private final SystemNotificationService systemNotificationService;

    public CurriculumSuggestionServiceImpl(CurriculumSuggestionRepository suggestionRepository,
                                           CurriculumSuggestionWindowRepository suggestionWindowRepository,
                                           UserRepository userRepository,
                                           SystemNotificationService systemNotificationService) {
        this.suggestionRepository = suggestionRepository;
        this.suggestionWindowRepository = suggestionWindowRepository;
        this.userRepository = userRepository;
        this.systemNotificationService = systemNotificationService;
    }

    @Override
    public SuggestionResponseDTO createSuggestion(SuggestionRequestDTO requestDTO, String requesterRole) {
        if (!isSubmissionOpen() && "MENTOR".equalsIgnoreCase(requesterRole)) {
            throw new IllegalStateException("Curriculum suggestions are currently closed. Wait until HOD requests new suggestions.");
        }

        if (suggestionRepository.existsByMentorIdAndSuggestionTitle(
                requestDTO.getMentorId(), requestDTO.getSuggestionTitle())) {
            throw new DuplicateSuggestionException(
                    "A suggestion with the same title already exists for this mentor.");
        }

        SuggestionCategory category;
        try {
            category = SuggestionCategory.valueOf(requestDTO.getCategory().toUpperCase().replace(" ", "_"));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid category: " + requestDTO.getCategory() +
                    ". Must be NEW_COURSE, COURSE_IMPROVEMENT, SKILL_DEVELOPMENT, or TECHNOLOGY_UPDATE.");
        }

        CurriculumSuggestion suggestion = new CurriculumSuggestion();
        suggestion.setMentorId(requestDTO.getMentorId());
        suggestion.setSuggestionTitle(requestDTO.getSuggestionTitle());
        suggestion.setCategory(category);
        suggestion.setDescription(requestDTO.getDescription());
        suggestion.setSuggestedCourse(requestDTO.getSuggestedCourse());

        CurriculumSuggestion saved = suggestionRepository.save(suggestion);
        notifyHodWorkspace(saved);
        return mapToResponseDTO(saved);
    }

    @Override
    public List<SuggestionResponseDTO> getSuggestionsByMentor(Integer mentorId) {
        return suggestionRepository.findByMentorIdOrderBySubmissionDateDesc(mentorId)
                .stream().map(this::mapToResponseDTO).collect(Collectors.toList());
    }

    @Override
    public List<SuggestionResponseDTO> getAllSuggestions() {
        return suggestionRepository.findAllByOrderBySubmissionDateDesc()
                .stream().map(this::mapToResponseDTO).collect(Collectors.toList());
    }

    @Override
    public SuggestionResponseDTO updateSuggestionStatus(Integer suggestionId, String reviewStatus) {
        CurriculumSuggestion suggestion = suggestionRepository.findById(suggestionId)
                .orElseThrow(() -> new IllegalArgumentException("Suggestion not found."));

        ReviewStatus status;
        try {
            status = ReviewStatus.valueOf((reviewStatus == null ? "" : reviewStatus).trim().toUpperCase(Locale.ROOT));
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid review status. Use PENDING or REVIEWED.");
        }

        suggestion.setReviewStatus(status);
        CurriculumSuggestion saved = suggestionRepository.save(suggestion);

        if (saved.getMentorId() != null) {
            systemNotificationService.createNotification(
                    saved.getMentorId().longValue(),
                    "Curriculum Suggestion Updated",
                    "HOD reviewed the suggestion \"" + saved.getSuggestionTitle() + "\". Status: " + saved.getReviewStatus().name(),
                    "/industry-mentor/suggestions"
            );
        }

        return mapToResponseDTO(saved);
    }

    @Override
    public boolean isSubmissionOpen() {
        return getOrCreateWindow().isSubmissionsOpen();
    }

    @Override
    public boolean setSubmissionOpen(boolean open, Long actingUserId) {
        CurriculumSuggestionWindow window = getOrCreateWindow();
        window.setSubmissionsOpen(open);
        window.setUpdatedByUserId(actingUserId);
        suggestionWindowRepository.save(window);

        notifyIndustryMentors(open);
        return window.isSubmissionsOpen();
    }

    private CurriculumSuggestionWindow getOrCreateWindow() {
        return suggestionWindowRepository.findTopByOrderByUpdatedAtDesc().orElseGet(() -> {
            CurriculumSuggestionWindow created = new CurriculumSuggestionWindow();
            created.setSubmissionsOpen(false);
            created.setUpdatedByUserId(null);
            return suggestionWindowRepository.save(created);
        });
    }

    private void notifyIndustryMentors(boolean open) {
        List<User> industryMentors = userRepository.findByRoleAndActiveTrue(Role.INDUSTRY_MENTOR);
        for (User mentor : industryMentors) {
            if (mentor.getId() == null) {
                continue;
            }
            systemNotificationService.createNotification(
                    mentor.getId(),
                    open ? "Curriculum Suggestions Requested" : "Curriculum Suggestions Closed",
                    open
                            ? "HOD requested new curriculum suggestions. You can submit them now."
                            : "HOD closed curriculum suggestion submissions for now.",
                    "/industry-mentor/suggestions"
            );
        }
    }

    private void notifyHodWorkspace(CurriculumSuggestion suggestion) {
        User mentor = suggestion.getMentorId() == null
                ? null
                : userRepository.findById(suggestion.getMentorId().longValue()).orElse(null);

        String mentorDepartment = mentor == null ? null : mentor.getDepartment();
        String mentorName = mentor == null ? "An industry mentor" : mentor.getFullName();
        String message = mentorName + " submitted a curriculum suggestion: \"" + suggestion.getSuggestionTitle() + "\".";

        List<User> recipients = userRepository.findByRoleAndActiveTrue(Role.DEPARTMENT_HEAD).stream()
                .filter(user -> sameDepartment(user.getDepartment(), mentorDepartment))
                .collect(Collectors.toList());
        recipients.addAll(
                userRepository.findByRoleAndActiveTrue(Role.DEPARTMENT_ASSISTANT).stream()
                        .filter(user -> sameDepartment(user.getDepartment(), mentorDepartment))
                        .toList()
        );

        for (User recipient : recipients) {
            if (recipient.getId() == null) {
                continue;
            }
            systemNotificationService.createNotification(
                    recipient.getId(),
                    "New Curriculum Suggestion",
                    message,
                    "/hod/curriculum-suggestions"
            );
        }
    }

    private boolean sameDepartment(String left, String right) {
        if (left == null || left.trim().isEmpty() || right == null || right.trim().isEmpty()) {
            return true;
        }
        return left.trim().equalsIgnoreCase(right.trim());
    }

    private SuggestionResponseDTO mapToResponseDTO(CurriculumSuggestion s) {
        return SuggestionResponseDTO.builder()
                .suggestionId(s.getSuggestionId())
                .mentorId(s.getMentorId())
                .suggestionTitle(s.getSuggestionTitle())
                .category(s.getCategory().name())
                .description(s.getDescription())
                .suggestedCourse(s.getSuggestedCourse())
                .submissionDate(s.getSubmissionDate())
                .reviewStatus(s.getReviewStatus().name())
                .build();
    }
}
