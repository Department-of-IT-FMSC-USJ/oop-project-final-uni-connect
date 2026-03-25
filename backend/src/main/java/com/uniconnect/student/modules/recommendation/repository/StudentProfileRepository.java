package com.uniconnect.student.modules.recommendation.repository;

import com.uniconnect.student.modules.recommendation.entity.StudentProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for StudentProfile entity.
 */
@Repository
public interface StudentProfileRepository extends JpaRepository<StudentProfile, Integer> {

    /**
     * Find a student profile by student ID.
     */
    Optional<StudentProfile> findByStudentId(Integer studentId);

    /**
     * Check if a student profile exists.
     */
    boolean existsByStudentId(Integer studentId);
}
