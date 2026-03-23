package com.uniconnect.student.modules.feedback.repository;

import com.uniconnect.student.modules.feedback.entity.MentorSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for MentorSession entity.
 * Used as a read reference to verify session completion.
 */
@Repository
public interface MentorSessionRepository extends JpaRepository<MentorSession, Integer> {

    /**
     * Fetch all mentor feedback sessions for a student ordered by newest session first.
     */
    List<MentorSession> findByStudentIdOrderBySessionDateDesc(Integer studentId);
}
