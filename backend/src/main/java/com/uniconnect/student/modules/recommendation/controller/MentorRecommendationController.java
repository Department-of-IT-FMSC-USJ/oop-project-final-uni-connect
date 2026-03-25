package com.uniconnect.student.modules.recommendation.controller;

import com.uniconnect.student.common.dto.ApiResponseDTO;
import com.uniconnect.student.modules.recommendation.dto.MentorProfileRequestDTO;
import com.uniconnect.student.modules.recommendation.dto.MentorProfileResponseDTO;
import com.uniconnect.student.modules.recommendation.dto.RecommendationResponseDTO;
import com.uniconnect.student.modules.recommendation.dto.StudentProfileRequestDTO;
import com.uniconnect.student.modules.recommendation.service.MentorRecommendationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for Mentor Recommendation operations.
 * Provides endpoints for managing student/mentor profiles and generating recommendations.
 */
@RestController
@RequestMapping("/api/recommendations")
public class MentorRecommendationController {

    private final MentorRecommendationService recommendationService;

    public MentorRecommendationController(MentorRecommendationService recommendationService) {
        this.recommendationService = recommendationService;
    }

    /**
     * Save or update a student profile with interest tags.
     *
     * POST /api/recommendations/student-profile
     *
     * @param requestDTO the student profile data
     * @return success message
     */
    @PostMapping("/student-profile")
    public ResponseEntity<ApiResponseDTO<String>> saveStudentProfile(
            @Valid @RequestBody StudentProfileRequestDTO requestDTO) {

        String message = recommendationService.saveStudentProfile(requestDTO);

        ApiResponseDTO<String> response = ApiResponseDTO.success(message, null);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Save or update a mentor profile with expertise tags.
     *
     * POST /api/recommendations/mentor-profile
     *
     * @param requestDTO the mentor profile data
     * @return success message
     */
    @PostMapping("/mentor-profile")
    public ResponseEntity<ApiResponseDTO<String>> saveMentorProfile(
            @Valid @RequestBody MentorProfileRequestDTO requestDTO) {

        String message = recommendationService.saveMentorProfile(requestDTO);

        ApiResponseDTO<String> response = ApiResponseDTO.success(message, null);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Get a mentor's profile by mentor ID.
     *
     * GET /api/recommendations/mentor-profile/{mentorId}
     *
     * @param mentorId the mentor ID
     * @return mentor profile data
     */
    @GetMapping("/mentor-profile/{mentorId}")
    public ResponseEntity<ApiResponseDTO<MentorProfileResponseDTO>> getMentorProfile(
            @PathVariable("mentorId") Integer mentorId) {

        MentorProfileResponseDTO profile = recommendationService.getMentorProfile(mentorId);

        ApiResponseDTO<MentorProfileResponseDTO> response = ApiResponseDTO.success(
                "Mentor profile retrieved successfully", profile);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Generate mentor recommendations for a student using Jaccard Similarity.
     * This clears old recommendations and computes new ones.
     *
     * POST /api/recommendations/generate/{studentId}
     *
     * @param studentId the student ID
     * @return list of recommendations sorted by match score descending
     */
    @PostMapping("/generate/{studentId}")
    public ResponseEntity<ApiResponseDTO<List<RecommendationResponseDTO>>> generateRecommendations(
            @PathVariable("studentId") Integer studentId) {

        List<RecommendationResponseDTO> recommendations =
                recommendationService.generateRecommendations(studentId);

        ApiResponseDTO<List<RecommendationResponseDTO>> response = ApiResponseDTO.success(
                "Recommendations generated successfully", recommendations);

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Get existing recommendations for a student.
     *
     * GET /api/recommendations/{studentId}
     *
     * @param studentId the student ID
     * @return list of recommendations sorted by match score descending
     */
    @GetMapping("/{studentId}")
    public ResponseEntity<ApiResponseDTO<List<RecommendationResponseDTO>>> getRecommendations(
            @PathVariable("studentId") Integer studentId) {

        List<RecommendationResponseDTO> recommendations =
                recommendationService.getRecommendations(studentId);

        ApiResponseDTO<List<RecommendationResponseDTO>> response = ApiResponseDTO.success(
                "Recommendations retrieved successfully", recommendations);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
