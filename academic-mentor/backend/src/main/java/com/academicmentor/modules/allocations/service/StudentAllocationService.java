package com.academicmentor.modules.allocations.service;

import com.academicmentor.modules.allocations.dto.AllocationRequestDTO;
import com.academicmentor.modules.allocations.dto.AllocationResponseDTO;

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
}
