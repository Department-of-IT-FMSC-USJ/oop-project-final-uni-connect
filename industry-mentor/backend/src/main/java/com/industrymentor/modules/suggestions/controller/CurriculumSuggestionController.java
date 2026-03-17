package com.industrymentor.modules.suggestions.controller;

import com.industrymentor.common.dto.ApiResponseDTO;
import com.industrymentor.common.exception.UnauthorizedAccessException;
import com.industrymentor.modules.suggestions.dto.SuggestionRequestDTO;
import com.industrymentor.modules.suggestions.dto.SuggestionResponseDTO;
import com.industrymentor.modules.suggestions.service.CurriculumSuggestionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/suggestions")
public class CurriculumSuggestionController {

    private final CurriculumSuggestionService suggestionService;

    public CurriculumSuggestionController(CurriculumSuggestionService suggestionService) {
        this.suggestionService = suggestionService;
    }

    @PostMapping
    public ResponseEntity<ApiResponseDTO<SuggestionResponseDTO>> createSuggestion(
            @RequestHeader(value = "X-User-Role", defaultValue = "") String userRole,
            @Valid @RequestBody SuggestionRequestDTO requestDTO) {

        if (!"MENTOR".equalsIgnoreCase(userRole)) {
            throw new UnauthorizedAccessException("Only registered industry mentors can submit suggestions.");
        }

        SuggestionResponseDTO responseDTO = suggestionService.createSuggestion(requestDTO);
        ApiResponseDTO<SuggestionResponseDTO> response = ApiResponseDTO.success(
                "Curriculum suggestion submitted successfully", responseDTO);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/mentor/{mentorId}")
    public ResponseEntity<ApiResponseDTO<List<SuggestionResponseDTO>>> getSuggestionsByMentor(
            @PathVariable("mentorId") Integer mentorId,
            @RequestHeader(value = "X-User-Role", defaultValue = "") String userRole) {

        if (!"MENTOR".equalsIgnoreCase(userRole)) {
            throw new UnauthorizedAccessException("Only registered industry mentors can view suggestions.");
        }

        List<SuggestionResponseDTO> suggestions = suggestionService.getSuggestionsByMentor(mentorId);
        ApiResponseDTO<List<SuggestionResponseDTO>> response = ApiResponseDTO.success(
                "Suggestions retrieved successfully", suggestions);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
