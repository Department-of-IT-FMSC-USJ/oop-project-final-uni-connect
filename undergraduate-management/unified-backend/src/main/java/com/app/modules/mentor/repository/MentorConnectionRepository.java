package com.app.modules.mentor.repository;

import com.app.modules.mentor.entity.MentorConnection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for MentorConnection entity.
 * Extends JpaRepository to provide CRUD operations.
 */
@Repository
public interface MentorConnectionRepository extends JpaRepository<MentorConnection, Integer> {

    /**
     * Find all mentor connections for a specific student.
     *
     * @param studentId the student ID
     * @return list of mentor connections
     */
    List<MentorConnection> findByStudentIdOrderByCreatedDateDesc(Integer studentId);

    /**
     * Check if a connection already exists between a student and a mentor.
     *
     * @param studentId the student ID
     * @param mentorId  the mentor ID
     * @return true if connection exists
     */
    boolean existsByStudentIdAndMentorId(Integer studentId, Integer mentorId);
}
