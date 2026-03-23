package com.uniconnect.industry.modules.suggestions.service;

import com.uniconnect.industry.modules.suggestions.dto.SuggestionRequestDTO;
import com.uniconnect.industry.modules.suggestions.dto.SuggestionResponseDTO;

import java.util.List;

public interface CurriculumSuggestionService {

    SuggestionResponseDTO createSuggestion(SuggestionRequestDTO requestDTO);

    List<SuggestionResponseDTO> getSuggestionsByMentor(Integer mentorId);
}
