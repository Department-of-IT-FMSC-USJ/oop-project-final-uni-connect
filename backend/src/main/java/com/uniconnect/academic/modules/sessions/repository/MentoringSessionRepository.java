package com.uniconnect.academic.modules.sessions.repository;

import com.uniconnect.academic.modules.sessions.entity.MentoringSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * Repository interface for MentoringSession entity.
 */
@Repository("academicMentoringSessionRepository")
public interface MentoringSessionRepository extends JpaRepository<MentoringSession, Integer> {

    /**
     * Find all sessions for a specific mentor, ordered by session date descending.
     */
    List<MentoringSession> findByMentorIdOrderBySessionDateDescSessionTimeDesc(Integer mentorId);

    /**
     * Check if a mentor already has a session at the same date and time.
     * Used to prevent duplicate sessions.
     */
    boolean existsByMentorIdAndSessionDateAndSessionTime(Integer mentorId, LocalDate sessionDate, LocalTime sessionTime);
}
