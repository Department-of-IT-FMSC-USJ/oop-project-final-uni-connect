package com.uniconnect.student.modules.mentor.repository;

import com.uniconnect.student.modules.mentor.entity.MentorConnection;
import com.uniconnect.student.modules.mentor.enums.ConnectionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.uniconnect.student.modules.mentor.enums.MentorType;

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

    /**
     * Checks whether a student already has an approved connection to any mentor of the given type.
     */
    boolean existsByStudentIdAndMentorTypeAndConnectionStatus(
            Integer studentId,
            MentorType mentorType,
            ConnectionStatus connectionStatus
    );

    /**
     * Finds a specific approved connection for session provisioning.
     */
    java.util.Optional<MentorConnection> findByStudentIdAndMentorIdAndMentorTypeAndConnectionStatus(
            Integer studentId,
            Integer mentorId,
            MentorType mentorType,
            ConnectionStatus connectionStatus
    );

    boolean existsByStudentIdAndMentorIdAndConnectionStatus(
            Integer studentId,
            Integer mentorId,
            ConnectionStatus connectionStatus
    );

    /**
     * Finds all approved connections for a mentor (used to provision industry sessions).
     */
    List<MentorConnection> findByMentorIdAndMentorTypeAndConnectionStatus(
            Integer mentorId,
            MentorType mentorType,
            ConnectionStatus connectionStatus
    );
}
