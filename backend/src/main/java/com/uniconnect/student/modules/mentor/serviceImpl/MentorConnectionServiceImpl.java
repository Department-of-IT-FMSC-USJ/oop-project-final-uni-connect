package com.uniconnect.student.modules.mentor.serviceImpl;

import com.uniconnect.student.modules.mentor.dto.MentorConnectionRequestDTO;
import com.uniconnect.student.modules.mentor.dto.MentorConnectionResponseDTO;
import com.uniconnect.student.modules.mentor.entity.MentorConnection;
import com.uniconnect.student.modules.mentor.enums.ConnectionStatus;
import com.uniconnect.student.modules.mentor.enums.MentorType;
import com.uniconnect.student.modules.mentor.exception.InsufficientPointsException;
import com.uniconnect.student.modules.points.repository.ContributionPointsRepository;
import com.uniconnect.student.modules.mentor.repository.MentorConnectionRepository;
import com.uniconnect.student.modules.mentor.service.MentorConnectionService;
import com.uniconnect.model.User;
import com.uniconnect.repository.UserRepository;
import com.uniconnect.service.SystemNotificationService;
import com.uniconnect.student.modules.recommendation.dto.RecommendationResponseDTO;
import com.uniconnect.student.modules.recommendation.service.MentorRecommendationService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

/**
 * Implementation of MentorConnectionService.
 * Handles business logic for mentor allocation and connection handling.
 */
@Service
public class MentorConnectionServiceImpl implements MentorConnectionService {

    private final MentorConnectionRepository mentorConnectionRepository;
    private final ContributionPointsRepository contributionPointsRepository;
    private final MentorRecommendationService mentorRecommendationService;
    private final UserRepository userRepository;
    private final SystemNotificationService systemNotificationService;

    @Value("${mentor.industry.min-points}")
    private Integer industryMentorMinPoints;

    public MentorConnectionServiceImpl(MentorConnectionRepository mentorConnectionRepository,
                                        ContributionPointsRepository contributionPointsRepository,
                                        MentorRecommendationService mentorRecommendationService,
                                        UserRepository userRepository,
                                        SystemNotificationService systemNotificationService) {
        this.mentorConnectionRepository = mentorConnectionRepository;
        this.contributionPointsRepository = contributionPointsRepository;
        this.mentorRecommendationService = mentorRecommendationService;
        this.userRepository = userRepository;
        this.systemNotificationService = systemNotificationService;
    }

    /**
     * Request a mentor connection.
     * Academic mentors are available to all students.
     * Industry mentors require the student to have minimum contribution points.
     */
    @Override
    public MentorConnectionResponseDTO requestConnection(MentorConnectionRequestDTO requestDTO) {

        // Check if connection already exists between student and mentor
        if (mentorConnectionRepository.existsByStudentIdAndMentorId(
                requestDTO.getStudentId(), requestDTO.getMentorId())) {
            throw new IllegalStateException(
                    "A connection already exists between student " + requestDTO.getStudentId() +
                            " and mentor " + requestDTO.getMentorId());
        }

        // If requesting an Industry mentor, check student's contribution points
        if (requestDTO.getMentorType() == MentorType.Industry) {
            Integer totalPoints = contributionPointsRepository.getTotalPointsByStudentId(
                    requestDTO.getStudentId());
            if (totalPoints == null) {
                totalPoints = 0;
            }

            if (totalPoints < industryMentorMinPoints) {
                throw new InsufficientPointsException(
                        "Insufficient contribution points for Industry mentor connection. " +
                                "Required: " + industryMentorMinPoints +
                                ", Current: " + totalPoints);
            }
        }

        // Create and save the mentor connection
        MentorConnection connection = new MentorConnection();
        connection.setStudentId(requestDTO.getStudentId());
        connection.setMentorId(requestDTO.getMentorId());
        connection.setMentorType(requestDTO.getMentorType());
        connection.setConnectionStatus(ConnectionStatus.Pending);

        MentorConnection savedConnection = mentorConnectionRepository.save(connection);

        return mapToResponseDTO(savedConnection);
    }

    /**
     * Get all mentor connections for a specific student.
     */
    @Override
    public List<MentorConnectionResponseDTO> getConnectionsByStudent(Integer studentId) {
        List<MentorConnection> connections =
                mentorConnectionRepository.findByStudentIdOrderByCreatedDateDesc(studentId);

        return connections.stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public MentorConnectionResponseDTO autoAssignIndustryMentor(Integer studentId) {
        User student = userRepository.findById(studentId.longValue())
                .orElseThrow(() -> new IllegalArgumentException("Student not found."));

        Integer totalPoints = Optional.ofNullable(contributionPointsRepository.getTotalPointsByStudentId(studentId)).orElse(0);
        if (totalPoints < industryMentorMinPoints) {
            throw new InsufficientPointsException(
                    "Industry mentor recommendation requires at least " + industryMentorMinPoints + " points.");
        }

        List<MentorConnection> approvedIndustryConnections = mentorConnectionRepository.findByStudentIdOrderByCreatedDateDesc(studentId)
                .stream()
                .filter(connection -> connection.getMentorType() == MentorType.Industry)
                .filter(connection -> connection.getConnectionStatus() == ConnectionStatus.Approved)
                .toList();
        if (!approvedIndustryConnections.isEmpty()) {
            return mapToResponseDTO(approvedIndustryConnections.get(0));
        }

        List<RecommendationResponseDTO> recommendations = mentorRecommendationService.generateRecommendations(studentId);
        if (recommendations == null || recommendations.isEmpty()) {
            throw new IllegalStateException(
                    "No matching industry mentor found. Update your interests or wait for mentor profiles.");
        }

        int topScore = recommendations.stream()
                .map(recommendation -> Optional.ofNullable(recommendation.getMatchScore()).orElse(0))
                .max(Comparator.naturalOrder())
                .orElse(0);

        List<RecommendationResponseDTO> topRecommendations = recommendations.stream()
                .filter(recommendation -> Optional.ofNullable(recommendation.getMatchScore()).orElse(0) == topScore)
                .toList();
        RecommendationResponseDTO chosen = topRecommendations.get(ThreadLocalRandom.current().nextInt(topRecommendations.size()));

        if (chosen.getMentorId() == null) {
            throw new IllegalStateException("No eligible industry mentor available right now.");
        }

        MentorConnection connection = new MentorConnection();
        connection.setStudentId(studentId);
        connection.setMentorId(chosen.getMentorId());
        connection.setMentorType(MentorType.Industry);
        connection.setConnectionStatus(ConnectionStatus.Approved);

        MentorConnection saved = mentorConnectionRepository.save(connection);

        systemNotificationService.createNotification(
                student.getId(),
                "Industry Mentor Assigned",
                "You have been matched with " + chosen.getMentorName() + " (" + topScore + "% match).",
                "/undergraduate/mentors"
        );

        systemNotificationService.createNotification(
                chosen.getMentorId().longValue(),
                "New Student Match",
                student.getFullName() + " was matched with you through the recommendation flow.",
                "/industry-mentor/students"
        );

        return mapToResponseDTO(saved);
    }

    @Override
    public List<MentorConnectionResponseDTO> getConnectionsByMentor(Integer mentorId) {
        List<MentorConnection> connections = mentorConnectionRepository.findAll().stream()
                .filter(connection -> mentorId.equals(connection.getMentorId()))
                .sorted((left, right) -> right.getCreatedDate().compareTo(left.getCreatedDate()))
                .toList();

        return connections.stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Map MentorConnection entity to MentorConnectionResponseDTO.
     *
     * @param connection the entity to map
     * @return the response DTO
     */
    private MentorConnectionResponseDTO mapToResponseDTO(MentorConnection connection) {
        return MentorConnectionResponseDTO.builder()
                .connectionId(connection.getConnectionId())
                .studentId(connection.getStudentId())
                .mentorId(connection.getMentorId())
                .mentorType(connection.getMentorType())
                .connectionStatus(connection.getConnectionStatus())
                .createdDate(connection.getCreatedDate())
                .build();
    }
}
