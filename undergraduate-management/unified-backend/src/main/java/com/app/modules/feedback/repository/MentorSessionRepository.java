package com.app.modules.feedback.repository;

import com.app.modules.feedback.entity.MentorSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for MentorSession entity.
 * Used as a read reference to verify session completion.
 */
@Repository
public interface MentorSessionRepository extends JpaRepository<MentorSession, Integer> {
}
