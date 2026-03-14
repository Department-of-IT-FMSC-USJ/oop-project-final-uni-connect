package com.app.modules.recommendation.serviceImpl;

import com.app.modules.recommendation.dto.MentorProfileRequestDTO;
import com.app.modules.recommendation.dto.RecommendationResponseDTO;
import com.app.modules.recommendation.dto.StudentProfileRequestDTO;
import com.app.modules.recommendation.entity.MentorProfile;
import com.app.modules.recommendation.entity.MentorRecommendation;
import com.app.modules.recommendation.entity.StudentProfile;
import com.app.modules.recommendation.enums.MentorCategory;
import com.app.modules.recommendation.exception.ProfileIncompleteException;
import com.app.modules.recommendation.repository.MentorProfileRepository;
import com.app.modules.recommendation.repository.MentorRecommendationRepository;
import com.app.modules.recommendation.repository.StudentProfileRepository;
import com.app.modules.recommendation.service.MentorRecommendationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementation of MentorRecommendationService.
 * Uses Jaccard Similarity to match student interest tags with mentor expertise tags,
 * then ranks mentors by highest match score.
 */
@Service
public class MentorRecommendationServiceImpl implements MentorRecommendationService {

    private final StudentProfileRepository studentProfileRepository;
    private final MentorProfileRepository mentorProfileRepository;
    private final MentorRecommendationRepository recommendationRepository;

    public MentorRecommendationServiceImpl(StudentProfileRepository studentProfileRepository,
                                            MentorProfileRepository mentorProfileRepository,
                                            MentorRecommendationRepository recommendationRepository) {
        this.studentProfileRepository = studentProfileRepository;
        this.mentorProfileRepository = mentorProfileRepository;
        this.recommendationRepository = recommendationRepository;
    }

    /**
     * Save or update a student profile with interest tags.
     */
    @Override
    public String saveStudentProfile(StudentProfileRequestDTO requestDTO) {
        Optional<StudentProfile> existing = studentProfileRepository.findByStudentId(requestDTO.getStudentId());

        StudentProfile profile;
        if (existing.isPresent()) {
            profile = existing.get();
            profile.setStudentName(requestDTO.getStudentName());
            profile.setDepartment(requestDTO.getDepartment());
            profile.setInterestTags(requestDTO.getInterestTags());
        } else {
            profile = new StudentProfile();
            profile.setStudentId(requestDTO.getStudentId());
            profile.setStudentName(requestDTO.getStudentName());
            profile.setDepartment(requestDTO.getDepartment());
            profile.setInterestTags(requestDTO.getInterestTags());
        }

        studentProfileRepository.save(profile);
        return "Student profile saved successfully";
    }

    /**
     * Save or update a mentor profile with expertise tags.
     */
    @Override
    public String saveMentorProfile(MentorProfileRequestDTO requestDTO) {
        Optional<MentorProfile> existing = mentorProfileRepository.findByMentorId(requestDTO.getMentorId());
        MentorCategory category = MentorCategory.valueOf(requestDTO.getMentorCategory());

        MentorProfile profile;
        if (existing.isPresent()) {
            profile = existing.get();
            profile.setMentorName(requestDTO.getMentorName());
            profile.setMentorCategory(category);
            profile.setExpertiseTags(requestDTO.getExpertiseTags());
            profile.setDepartment(requestDTO.getDepartment());
        } else {
            profile = new MentorProfile();
            profile.setMentorId(requestDTO.getMentorId());
            profile.setMentorName(requestDTO.getMentorName());
            profile.setMentorCategory(category);
            profile.setExpertiseTags(requestDTO.getExpertiseTags());
            profile.setDepartment(requestDTO.getDepartment());
            profile.setIsAvailable(true);
        }

        mentorProfileRepository.save(profile);
        return "Mentor profile saved successfully";
    }

    /**
     * Generate mentor recommendations for a student using Jaccard Similarity.
     * Steps:
     * 1. Verify student profile exists with interest tags
     * 2. Get all available mentors
     * 3. Compute Jaccard Similarity between student interests and each mentor's expertise
     * 4. Convert to percentage score (0-100)
     * 5. Store recommendations sorted by highest score
     */
    @Override
    @Transactional
    public List<RecommendationResponseDTO> generateRecommendations(Integer studentId) {

        // 1. Verify student profile
        StudentProfile studentProfile = studentProfileRepository.findByStudentId(studentId)
                .orElseThrow(() -> new ProfileIncompleteException(
                        "Student profile not found. Please complete your profile before generating recommendations."));

        if (studentProfile.getInterestTags() == null || studentProfile.getInterestTags().trim().isEmpty()) {
            throw new ProfileIncompleteException(
                    "Student interest tags are empty. Please add your interests before generating recommendations.");
        }

        // 2. Get all available mentors
        List<MentorProfile> availableMentors = mentorProfileRepository.findByIsAvailableTrue();

        if (availableMentors.isEmpty()) {
            return Collections.emptyList();
        }

        // Parse student interest tags into a set (lowercased, trimmed)
        Set<String> studentTags = parseTags(studentProfile.getInterestTags());

        // 3. Delete old recommendations for this student
        recommendationRepository.deleteByStudentId(studentId);

        // 4. Compute Jaccard Similarity for each mentor and save recommendations
        List<MentorRecommendation> recommendations = new ArrayList<>();

        for (MentorProfile mentor : availableMentors) {
            Set<String> mentorTags = parseTags(mentor.getExpertiseTags());

            double jaccardScore = computeJaccardSimilarity(studentTags, mentorTags);
            int percentageScore = (int) Math.round(jaccardScore * 100);

            // Only recommend mentors with a score > 0
            if (percentageScore > 0) {
                MentorRecommendation recommendation = new MentorRecommendation();
                recommendation.setStudentId(studentId);
                recommendation.setMentorId(mentor.getMentorId());
                recommendation.setMatchScore(percentageScore);

                recommendations.add(recommendation);
            }
        }

        // 5. Save all and return sorted by score descending
        List<MentorRecommendation> savedRecommendations = recommendationRepository.saveAll(recommendations);

        return savedRecommendations.stream()
                .sorted(Comparator.comparingInt(MentorRecommendation::getMatchScore).reversed())
                .map(rec -> mapToResponseDTO(rec, studentProfile, availableMentors))
                .collect(Collectors.toList());
    }

    /**
     * Get existing recommendations for a student, sorted by match score descending.
     */
    @Override
    public List<RecommendationResponseDTO> getRecommendations(Integer studentId) {

        // Verify student profile exists
        StudentProfile studentProfile = studentProfileRepository.findByStudentId(studentId)
                .orElseThrow(() -> new ProfileIncompleteException(
                        "Student profile not found. Please complete your profile first."));

        List<MentorRecommendation> recommendations =
                recommendationRepository.findByStudentIdOrderByMatchScoreDesc(studentId);

        // Load all mentor profiles for mapping
        List<MentorProfile> allMentors = mentorProfileRepository.findAll();

        return recommendations.stream()
                .map(rec -> mapToResponseDTO(rec, studentProfile, allMentors))
                .collect(Collectors.toList());
    }

    /**
     * Compute Jaccard Similarity between two sets of tags.
     * Jaccard(A, B) = |A ∩ B| / |A ∪ B|
     *
     * @param setA first set of tags
     * @param setB second set of tags
     * @return similarity score between 0.0 and 1.0
     */
    private double computeJaccardSimilarity(Set<String> setA, Set<String> setB) {
        if (setA.isEmpty() && setB.isEmpty()) {
            return 0.0;
        }

        Set<String> intersection = new HashSet<>(setA);
        intersection.retainAll(setB);

        Set<String> union = new HashSet<>(setA);
        union.addAll(setB);

        if (union.isEmpty()) {
            return 0.0;
        }

        return (double) intersection.size() / union.size();
    }

    /**
     * Parse a comma-separated tag string into a set of lowercased, trimmed tags.
     *
     * @param tagsString comma-separated tags (e.g. "Java, Machine Learning, Spring Boot")
     * @return set of normalized tag strings
     */
    private Set<String> parseTags(String tagsString) {
        if (tagsString == null || tagsString.trim().isEmpty()) {
            return Collections.emptySet();
        }

        return Arrays.stream(tagsString.split(","))
                .map(String::trim)
                .map(String::toLowerCase)
                .filter(tag -> !tag.isEmpty())
                .collect(Collectors.toSet());
    }

    /**
     * Find the intersection of student interests and mentor expertise as displayable tags.
     */
    private List<String> findMatchingTags(String studentTags, String mentorTags) {
        Set<String> studentSet = parseTags(studentTags);
        Set<String> mentorSet = parseTags(mentorTags);

        Set<String> intersection = new HashSet<>(studentSet);
        intersection.retainAll(mentorSet);

        return new ArrayList<>(intersection);
    }

    /**
     * Map a MentorRecommendation entity to a RecommendationResponseDTO.
     */
    private RecommendationResponseDTO mapToResponseDTO(MentorRecommendation recommendation,
                                                        StudentProfile studentProfile,
                                                        List<MentorProfile> mentors) {
        // Find the mentor profile for this recommendation
        MentorProfile mentorProfile = mentors.stream()
                .filter(m -> m.getMentorId().equals(recommendation.getMentorId()))
                .findFirst()
                .orElse(null);

        String mentorName = mentorProfile != null ? mentorProfile.getMentorName() : "Unknown Mentor";
        String mentorCategory = mentorProfile != null ? mentorProfile.getMentorCategory().getDisplayName() : "Unknown";
        String expertiseArea = mentorProfile != null ? mentorProfile.getExpertiseTags() : "";

        List<String> matchingTags = mentorProfile != null
                ? findMatchingTags(studentProfile.getInterestTags(), mentorProfile.getExpertiseTags())
                : Collections.emptyList();

        return RecommendationResponseDTO.builder()
                .recommendationId(recommendation.getRecommendationId())
                .studentId(recommendation.getStudentId())
                .mentorId(recommendation.getMentorId())
                .mentorName(mentorName)
                .mentorCategory(mentorCategory)
                .expertiseArea(expertiseArea)
                .matchingTags(matchingTags)
                .matchScore(recommendation.getMatchScore())
                .recommendationDate(recommendation.getRecommendationDate())
                .build();
    }
}
