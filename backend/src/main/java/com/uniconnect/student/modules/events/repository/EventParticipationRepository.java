package com.uniconnect.student.modules.events.repository;

import com.uniconnect.student.modules.events.entity.EventParticipation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for EventParticipation entity.
 * Extends JpaRepository to provide CRUD operations.
 */
@Repository
public interface EventParticipationRepository extends JpaRepository<EventParticipation, Integer> {

    /**
     * Find all event participations by a specific student.
     *
     * @param studentId the student ID
     * @return list of event participations
     */
    List<EventParticipation> findByStudentIdOrderBySubmittedDateDesc(Integer studentId);

    /**
     * Check if a student has already submitted participation for a specific event.
     * Used to prevent duplicate event submissions by the same student.
     *
     * @param studentId the student ID
     * @param eventName the event name
     * @return true if a duplicate exists
     */
    boolean existsByStudentIdAndEventName(Integer studentId, String eventName);
}
