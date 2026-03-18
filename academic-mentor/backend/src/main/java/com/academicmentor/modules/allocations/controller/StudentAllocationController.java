package com.academicmentor.modules.allocations.controller;

import com.academicmentor.common.dto.ApiResponseDTO;
import com.academicmentor.modules.allocations.dto.AllocationRequestDTO;
import com.academicmentor.modules.allocations.dto.AllocationResponseDTO;
import com.academicmentor.modules.allocations.service.StudentAllocationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for Student Allocation operations.
 * Provides endpoints for allocating students and viewing allocations.
 */
@RestController
@RequestMapping("/api/allocations")
public class StudentAllocationController {

    private final StudentAllocationService studentAllocationService;

    public StudentAllocationController(StudentAllocationService studentAllocationService) {
        this.studentAllocationService = studentAllocationService;
    }

    /**
     * Allocate a student to a mentor.
     *
     * POST /api/allocations
     */
    @PostMapping
    public ResponseEntity<ApiResponseDTO<AllocationResponseDTO>> allocateStudent(
            @Valid @RequestBody AllocationRequestDTO requestDTO) {

        AllocationResponseDTO responseDTO = studentAllocationService.allocateStudent(requestDTO);

        ApiResponseDTO<AllocationResponseDTO> response = ApiResponseDTO.success(
                "Student allocated successfully", responseDTO);

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Get all students allocated to a specific mentor.
     *
     * GET /api/allocations/mentor/{mentorId}
     */
    @GetMapping("/mentor/{mentorId}")
    public ResponseEntity<ApiResponseDTO<List<AllocationResponseDTO>>> getAllocationsByMentor(
            @PathVariable("mentorId") Integer mentorId) {

        List<AllocationResponseDTO> allocations = studentAllocationService.getAllocationsByMentor(mentorId);

        ApiResponseDTO<List<AllocationResponseDTO>> response = ApiResponseDTO.success(
                "Allocations retrieved successfully", allocations);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
