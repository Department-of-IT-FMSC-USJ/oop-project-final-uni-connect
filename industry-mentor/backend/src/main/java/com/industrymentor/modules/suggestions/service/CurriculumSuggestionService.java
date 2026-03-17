package com.industrymentor.modules.suggestions.service;

import com.industrymentor.modules.suggestions.dto.SuggestionRequestDTO;
import com.industrymentor.modules.suggestions.dto.SuggestionResponseDTO;

import java.util.List;

public interface CurriculumSuggestionService {

    SuggestionResponseDTO createSuggestion(SuggestionRequestDTO requestDTO);

    List<SuggestionResponseDTO> getSuggestionsByMentor(Integer mentorId);
}
