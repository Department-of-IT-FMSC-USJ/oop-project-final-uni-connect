package com.app.modules.points.serviceImpl;

import com.app.modules.points.dto.ContributionPointsResponseDTO;
import com.app.modules.points.dto.StudentTotalPointsDTO;
import com.app.modules.points.entity.ContributionPoints;
import com.app.modules.events.entity.EventParticipation;
import com.app.modules.points.enums.ActivityType;
import com.app.modules.events.enums.VerificationStatus;
import com.app.modules.points.exception.DuplicateAllocationException;
import com.app.common.exception.ResourceNotFoundException;
import com.app.modules.points.repository.ContributionPointsRepository;
import com.app.modules.events.repository.EventParticipationRepository;
import com.app.modules.points.service.ContributionPointsService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of ContributionPointsService.
 * Handles business logic for contribution point calculation and allocation.
 */
@Service
public class ContributionPointsServiceImpl implements ContributionPointsService {

    private final ContributionPointsRepository contributionPointsRepository;
    private final EventParticipationRepository eventParticipationRepository;

    public ContributionPointsServiceImpl(ContributionPointsRepository contributionPointsRepository,
                                          EventParticipationRepository eventParticipationRepository) {
        this.contributionPointsRepository = contributionPointsRepository;
        this.eventParticipationRepository = eventParticipationRepository;
    }

    /**
     * Calculate and allocate points after an event participation is verified (approved).
     * Validates that the participation exists, is approved, and points haven't been allocated already.
     */
    @Override
    public ContributionPointsResponseDTO calculatePointsAfterVerification(Integer participationId,
                                                                            ActivityType activityType) {

        // Find the event participation
        EventParticipation participation = eventParticipationRepository.findById(participationId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Event participation not found with ID: " + participationId));

        // Check if the participation is approved
        if (participation.getVerificationStatus() != VerificationStatus.Approved) {
            throw new IllegalStateException(
                    "Event participation is not approved. Current status: " + participation.getVerificationStatus());
        }

        // Prevent duplicate point allocation
        if (contributionPointsRepository.existsByStudentIdAndActivityReferenceId(
                participation.getStudentId(), participationId)) {
            throw new DuplicateAllocationException(
                    "Points have already been allocated for participation ID: " + participationId);
        }

        // Calculate points based on activity type
        int pointsEarned = activityType.getDefaultPoints();

        // Get current total points for the student
        Integer currentTotal = contributionPointsRepository.getTotalPointsByStudentId(
                participation.getStudentId());
        int updatedTotal = currentTotal + pointsEarned;

        // Create and save the contribution points record
        ContributionPoints contributionPoints = new ContributionPoints();
        contributionPoints.setStudentId(participation.getStudentId());
        contributionPoints.setPointsEarned(pointsEarned);
        contributionPoints.setActivityType(activityType);
        contributionPoints.setActivityReferenceId(participationId);
        contributionPoints.setUpdatedTotalPoints(updatedTotal);

        ContributionPoints savedPoints = contributionPointsRepository.save(contributionPoints);

        return mapToResponseDTO(savedPoints);
    }

    /**
     * Get all contribution points records for a specific student.
     */
    @Override
    public List<ContributionPointsResponseDTO> getPointsByStudent(Integer studentId) {
        List<ContributionPoints> pointsList =
                contributionPointsRepository.findByStudentIdOrderByCreatedDateDesc(studentId);

        return pointsList.stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get the total contribution points for a specific student.
     */
    @Override
    public StudentTotalPointsDTO getTotalPoints(Integer studentId) {
        Integer totalPoints = contributionPointsRepository.getTotalPointsByStudentId(studentId);
        return new StudentTotalPointsDTO(studentId, totalPoints);
    }

    /**
     * Map ContributionPoints entity to ContributionPointsResponseDTO.
     *
     * @param points the entity to map
     * @return the response DTO
     */
    private ContributionPointsResponseDTO mapToResponseDTO(ContributionPoints points) {
        return ContributionPointsResponseDTO.builder()
                .pointId(points.getPointId())
                .studentId(points.getStudentId())
                .pointsEarned(points.getPointsEarned())
                .activityType(points.getActivityType())
                .activityReferenceId(points.getActivityReferenceId())
                .updatedTotalPoints(points.getUpdatedTotalPoints())
                .createdDate(points.getCreatedDate())
                .build();
    }
}
