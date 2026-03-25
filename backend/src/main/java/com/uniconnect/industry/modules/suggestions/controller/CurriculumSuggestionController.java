package com.uniconnect.industry.modules.suggestions.controller;

import com.uniconnect.industry.common.dto.ApiResponseDTO;
import com.uniconnect.industry.common.exception.UnauthorizedAccessException;
import com.uniconnect.industry.modules.suggestions.dto.SuggestionRequestDTO;
import com.uniconnect.industry.modules.suggestions.dto.SuggestionResponseDTO;
import com.uniconnect.industry.modules.suggestions.service.CurriculumSuggestionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

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

        SuggestionResponseDTO responseDTO = suggestionService.createSuggestion(requestDTO, userRole);
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

    @GetMapping("/all")
    public ResponseEntity<ApiResponseDTO<List<SuggestionResponseDTO>>> getAllSuggestions(
            @RequestHeader(value = "X-User-Role", defaultValue = "") String userRole) {

        if (!"DEPARTMENT_HEAD".equalsIgnoreCase(userRole) && !"DEPARTMENT_ASSISTANT".equalsIgnoreCase(userRole)) {
            throw new UnauthorizedAccessException("Only HOD workspace users can view all suggestions.");
        }

        List<SuggestionResponseDTO> suggestions = suggestionService.getAllSuggestions();
        ApiResponseDTO<List<SuggestionResponseDTO>> response = ApiResponseDTO.success(
                "Suggestions retrieved successfully", suggestions);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PutMapping("/{suggestionId}/review")
    public ResponseEntity<ApiResponseDTO<SuggestionResponseDTO>> updateSuggestionReview(
            @RequestHeader(value = "X-User-Role", defaultValue = "") String userRole,
            @PathVariable("suggestionId") Integer suggestionId,
            @RequestBody Map<String, String> request) {

        if (!"DEPARTMENT_HEAD".equalsIgnoreCase(userRole) && !"DEPARTMENT_ASSISTANT".equalsIgnoreCase(userRole)) {
            throw new UnauthorizedAccessException("Only HOD workspace users can review suggestions.");
        }

        String reviewStatus = request == null ? null : request.get("reviewStatus");
        SuggestionResponseDTO responseDTO = suggestionService.updateSuggestionStatus(suggestionId, reviewStatus);
        ApiResponseDTO<SuggestionResponseDTO> response = ApiResponseDTO.success(
                "Suggestion review updated successfully", responseDTO);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/submission-window")
    public ResponseEntity<ApiResponseDTO<Map<String, Boolean>>> getSubmissionWindow() {
        boolean open = suggestionService.isSubmissionOpen();
        ApiResponseDTO<Map<String, Boolean>> response = ApiResponseDTO.success(
                "Curriculum suggestion submission window status retrieved successfully",
                Map.of("open", open));
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PutMapping("/submission-window")
    public ResponseEntity<ApiResponseDTO<Map<String, Boolean>>> setSubmissionWindow(
            @RequestHeader(value = "X-User-Role", defaultValue = "") String userRole,
            @RequestHeader(value = "X-User-Id", required = false) Long userId,
            @RequestBody Map<String, Boolean> request) {

        if (!"DEPARTMENT_HEAD".equalsIgnoreCase(userRole)) {
            throw new UnauthorizedAccessException("Only department heads can request curriculum suggestions.");
        }

        boolean open = request != null && Boolean.TRUE.equals(request.get("open"));
        boolean latest = suggestionService.setSubmissionOpen(open, userId);
        ApiResponseDTO<Map<String, Boolean>> response = ApiResponseDTO.success(
                "Curriculum suggestion submission window updated successfully",
                Map.of("open", latest));
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
