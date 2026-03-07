package com.app.modules.points.service;

import com.app.modules.points.dto.ContributionPointsResponseDTO;
import com.app.modules.points.dto.StudentTotalPointsDTO;
import com.app.modules.points.enums.ActivityType;

import java.util.List;

/**
 * Service interface for Contribution Points operations.
 */
public interface ContributionPointsService {

    /**
     * Calculate and allocate points after an event participation is verified (approved).
     *
     * @param participationId the ID of the approved event participation
     * @param activityType    the type of activity
     * @return the contribution points record
     */
    ContributionPointsResponseDTO calculatePointsAfterVerification(Integer participationId,
                                                                     ActivityType activityType);

    /**
     * Get all contribution points records for a specific student.
     *
     * @param studentId the student ID
     * @return list of contribution points records
     */
    List<ContributionPointsResponseDTO> getPointsByStudent(Integer studentId);

    /**
     * Get the total contribution points for a specific student.
     *
     * @param studentId the student ID
     * @return the student's total points
     */
    StudentTotalPointsDTO getTotalPoints(Integer studentId);
}
