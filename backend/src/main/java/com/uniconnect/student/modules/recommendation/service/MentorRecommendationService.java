package com.uniconnect.student.modules.recommendation.service;

import com.uniconnect.student.modules.recommendation.dto.MentorProfileRequestDTO;
import com.uniconnect.student.modules.recommendation.dto.MentorProfileResponseDTO;
import com.uniconnect.student.modules.recommendation.dto.RecommendationResponseDTO;
import com.uniconnect.student.modules.recommendation.dto.StudentProfileRequestDTO;

import java.util.List;

/**
 * Service interface for Mentor Recommendation operations.
 */
public interface MentorRecommendationService {

    /**
     * Save or update a student profile with interest tags.
     *
     * @param requestDTO the student profile data
     * @return success message
     */
    String saveStudentProfile(StudentProfileRequestDTO requestDTO);

    /**
     * Save or update a mentor profile with expertise tags.
     *
     * @param requestDTO the mentor profile data
     * @return success message
     */
    String saveMentorProfile(MentorProfileRequestDTO requestDTO);

    /**
     * Get a mentor's profile by mentor ID.
     *
     * @param mentorId the mentor ID
     * @return mentor profile data or null if not found
     */
    MentorProfileResponseDTO getMentorProfile(Integer mentorId);

    /**
     * Generate mentor recommendations for a student using Jaccard Similarity.
     * Clears previous recommendations and computes new ones.
     *
     * @param studentId the student ID
     * @return list of recommendation response DTOs sorted by match score descending
     */
    List<RecommendationResponseDTO> generateRecommendations(Integer studentId);

    /**
     * Get existing recommendations for a student.
     *
     * @param studentId the student ID
     * @return list of recommendation response DTOs sorted by match score descending
     */
    List<RecommendationResponseDTO> getRecommendations(Integer studentId);
}
