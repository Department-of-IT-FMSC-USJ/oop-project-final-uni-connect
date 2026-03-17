package com.industrymentor.modules.suggestions.serviceImpl;

import com.industrymentor.common.exception.DuplicateSuggestionException;
import com.industrymentor.modules.suggestions.dto.SuggestionRequestDTO;
import com.industrymentor.modules.suggestions.dto.SuggestionResponseDTO;
import com.industrymentor.modules.suggestions.entity.CurriculumSuggestion;
import com.industrymentor.modules.suggestions.enums.SuggestionCategory;
import com.industrymentor.modules.suggestions.repository.CurriculumSuggestionRepository;
import com.industrymentor.modules.suggestions.service.CurriculumSuggestionService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CurriculumSuggestionServiceImpl implements CurriculumSuggestionService {

    private final CurriculumSuggestionRepository suggestionRepository;

    public CurriculumSuggestionServiceImpl(CurriculumSuggestionRepository suggestionRepository) {
        this.suggestionRepository = suggestionRepository;
    }

    @Override
    public SuggestionResponseDTO createSuggestion(SuggestionRequestDTO requestDTO) {
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
        return mapToResponseDTO(saved);
    }

    @Override
    public List<SuggestionResponseDTO> getSuggestionsByMentor(Integer mentorId) {
        return suggestionRepository.findByMentorIdOrderBySubmissionDateDesc(mentorId)
                .stream().map(this::mapToResponseDTO).collect(Collectors.toList());
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
