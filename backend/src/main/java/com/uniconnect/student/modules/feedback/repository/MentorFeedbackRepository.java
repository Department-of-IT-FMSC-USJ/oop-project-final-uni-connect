package com.uniconnect.student.modules.feedback.repository;

import com.uniconnect.student.modules.feedback.entity.MentorFeedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for MentorFeedback entity.
 * Extends JpaRepository to provide CRUD operations.
 */
@Repository
public interface MentorFeedbackRepository extends JpaRepository<MentorFeedback, Integer> {

    /**
     * Find all feedback for a specific session.
     *
     * @param sessionId the session ID
     * @return list of feedback records
     */
    List<MentorFeedback> findBySessionIdOrderBySubmittedDateDesc(Integer sessionId);

    /**
     * Check if a student has already submitted feedback for a specific session.
     * Used to prevent duplicate feedback per session.
     *
     * @param sessionId the session ID
     * @param studentId the student ID
     * @return true if duplicate feedback exists
     */
    boolean existsBySessionIdAndStudentId(Integer sessionId, Integer studentId);
}
