package com.uniconnect.academic.modules.allocations.repository;

import com.uniconnect.academic.modules.allocations.entity.StudentAllocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for StudentAllocation entity.
 */
@Repository
public interface StudentAllocationRepository extends JpaRepository<StudentAllocation, Integer> {

    /**
     * Find all allocations for a specific mentor.
     */
    List<StudentAllocation> findByMentorIdOrderByAllocationDateDesc(Integer mentorId);

    /**
     * Check if a student is already allocated to any mentor.
     */
    boolean existsByStudentId(Integer studentId);

    /**
     * Count how many students are allocated to a specific mentor.
     */
    long countByMentorId(Integer mentorId);
}
