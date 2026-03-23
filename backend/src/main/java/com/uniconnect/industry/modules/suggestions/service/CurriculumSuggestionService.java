package com.uniconnect.industry.modules.suggestions.service;

import com.uniconnect.industry.modules.suggestions.dto.SuggestionRequestDTO;
import com.uniconnect.industry.modules.suggestions.dto.SuggestionResponseDTO;

import java.util.List;

public interface CurriculumSuggestionService {

    SuggestionResponseDTO createSuggestion(SuggestionRequestDTO requestDTO, String requesterRole);

    List<SuggestionResponseDTO> getSuggestionsByMentor(Integer mentorId);

    List<SuggestionResponseDTO> getAllSuggestions();

    SuggestionResponseDTO updateSuggestionStatus(Integer suggestionId, String reviewStatus);

    boolean isSubmissionOpen();

    boolean setSubmissionOpen(boolean open, Long actingUserId);
}
