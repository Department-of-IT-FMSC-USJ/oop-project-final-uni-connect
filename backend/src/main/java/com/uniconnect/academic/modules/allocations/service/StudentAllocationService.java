package com.uniconnect.academic.modules.allocations.service;

import com.uniconnect.academic.modules.allocations.dto.AllocationRequestDTO;
import com.uniconnect.academic.modules.allocations.dto.AllocationResponseDTO;

import java.util.List;

/**
 * Service interface for Student Allocation operations.
 */
public interface StudentAllocationService {

    /**
     * Allocate a student to a mentor.
     *
     * @param requestDTO the allocation details
     * @return the created allocation details
     */
    AllocationResponseDTO allocateStudent(AllocationRequestDTO requestDTO);

    /**
     * Get all student allocations for a specific mentor.
     *
     * @param mentorId the mentor ID
     * @return list of allocation records
     */
    List<AllocationResponseDTO> getAllocationsByMentor(Integer mentorId);

    /**
     * Automatically allocate an undergraduate student to an academic mentor.
     * Uses a least-loaded strategy with random tie-break.
     *
     * @param studentId the undergraduate user id
     * @return the created allocation record
     */
    AllocationResponseDTO allocateStudentAuto(Integer studentId);
}
