package com.uniconnect.student.modules.points.repository;

import com.uniconnect.student.modules.points.entity.ContributionPoints;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for ContributionPoints entity.
 * Extends JpaRepository to provide CRUD operations.
 */
@Repository
public interface ContributionPointsRepository extends JpaRepository<ContributionPoints, Integer> {

    /**
     * Find all contribution points records for a specific student.
     *
     * @param studentId the student ID
     * @return list of contribution points records
     */
    List<ContributionPoints> findByStudentIdOrderByCreatedDateDesc(Integer studentId);

    /**
     * Check if points have already been allocated for a specific activity reference.
     * Used to prevent duplicate point allocation.
     *
     * @param studentId           the student ID
     * @param activityReferenceId the activity reference ID
     * @return true if a duplicate exists
     */
    boolean existsByStudentIdAndActivityReferenceId(Integer studentId, Integer activityReferenceId);

    /**
     * Get the latest contribution points record for a student (to get the updated total).
     *
     * @param studentId the student ID
     * @return the latest contribution points record
     */
    Optional<ContributionPoints> findTopByStudentIdOrderByCreatedDateDesc(Integer studentId);

    /**
     * Get the total points for a student using a SUM query.
     *
     * @param studentId the student ID
     * @return the total points earned
     */
    @Query("SELECT COALESCE(SUM(cp.pointsEarned), 0) FROM ContributionPoints cp WHERE cp.studentId = :studentId")
    Integer getTotalPointsByStudentId(@Param("studentId") Integer studentId);
}
