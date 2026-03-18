package com.academicmentor.modules.allocations.serviceImpl;

import com.academicmentor.modules.allocations.dto.AllocationRequestDTO;
import com.academicmentor.modules.allocations.dto.AllocationResponseDTO;
import com.academicmentor.modules.allocations.entity.StudentAllocation;
import com.academicmentor.modules.allocations.exception.DuplicateAllocationException;
import com.academicmentor.modules.allocations.exception.MentorCapacityExceededException;
import com.academicmentor.modules.allocations.repository.StudentAllocationRepository;
import com.academicmentor.modules.allocations.service.StudentAllocationService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of StudentAllocationService.
 * Handles business logic for automatic student allocation to mentors.
 */
@Service
public class StudentAllocationServiceImpl implements StudentAllocationService {

    private final StudentAllocationRepository studentAllocationRepository;

    @Value("${mentor.max-student-capacity:10}")
    private int maxStudentCapacity;

    public StudentAllocationServiceImpl(StudentAllocationRepository studentAllocationRepository) {
        this.studentAllocationRepository = studentAllocationRepository;
    }

    /**
     * Allocate a student to a mentor.
     * Checks for duplicate allocation and mentor capacity.
     */
    @Override
    public AllocationResponseDTO allocateStudent(AllocationRequestDTO requestDTO) {

        // Ensure student is not already assigned
        if (studentAllocationRepository.existsByStudentId(requestDTO.getStudentId())) {
            throw new DuplicateAllocationException(
                    "Student with ID " + requestDTO.getStudentId() + " is already allocated to a mentor.");
        }

        // Check mentor capacity
        long currentCount = studentAllocationRepository.countByMentorId(requestDTO.getMentorId());
        if (currentCount >= maxStudentCapacity) {
            throw new MentorCapacityExceededException(
                    "Mentor with ID " + requestDTO.getMentorId() +
                            " has reached the maximum student capacity of " + maxStudentCapacity + ".");
        }

        // Create and save the allocation
        StudentAllocation allocation = new StudentAllocation();
        allocation.setMentorId(requestDTO.getMentorId());
        allocation.setStudentId(requestDTO.getStudentId());
        allocation.setDepartment(requestDTO.getDepartment());

        StudentAllocation savedAllocation = studentAllocationRepository.save(allocation);

        return mapToResponseDTO(savedAllocation);
    }

    /**
     * Get all student allocations for a specific mentor.
     */
    @Override
    public List<AllocationResponseDTO> getAllocationsByMentor(Integer mentorId) {
        List<StudentAllocation> allocations =
                studentAllocationRepository.findByMentorIdOrderByAllocationDateDesc(mentorId);

        return allocations.stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Map StudentAllocation entity to AllocationResponseDTO.
     */
    private AllocationResponseDTO mapToResponseDTO(StudentAllocation allocation) {
        return AllocationResponseDTO.builder()
                .allocationId(allocation.getAllocationId())
                .mentorId(allocation.getMentorId())
                .studentId(allocation.getStudentId())
                .department(allocation.getDepartment())
                .allocationDate(allocation.getAllocationDate())
                .build();
    }
}
