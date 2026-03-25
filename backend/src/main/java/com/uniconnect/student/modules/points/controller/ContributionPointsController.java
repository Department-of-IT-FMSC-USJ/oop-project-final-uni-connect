package com.uniconnect.student.modules.points.controller;

import com.uniconnect.student.common.dto.ApiResponseDTO;
import com.uniconnect.student.modules.points.dto.ContributionPointsRequestDTO;
import com.uniconnect.student.modules.points.dto.ContributionPointsResponseDTO;
import com.uniconnect.student.modules.points.dto.StudentTotalPointsDTO;
import com.uniconnect.student.modules.points.service.ContributionPointsService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for Contribution Points operations.
 * Provides endpoints for calculating, retrieving, and querying contribution points.
 */
@RestController
@RequestMapping("/api/points")
public class ContributionPointsController {

    private final ContributionPointsService contributionPointsService;

    public ContributionPointsController(ContributionPointsService contributionPointsService) {
        this.contributionPointsService = contributionPointsService;
    }

    /**
     * Calculate and allocate points after event participation verification.
     *
     * POST /api/contributions/calculate
     *
     * @param requestDTO the contribution points request containing participation ID and activity type
     * @return the allocated contribution points details
     */
    @PostMapping("/calculate")
    public ResponseEntity<ApiResponseDTO<ContributionPointsResponseDTO>> calculatePoints(
            @Valid @RequestBody ContributionPointsRequestDTO requestDTO) {

        ContributionPointsResponseDTO responseDTO = contributionPointsService
                .calculatePointsAfterVerification(requestDTO.getParticipationId(),
                        requestDTO.getActivityType());

        ApiResponseDTO<ContributionPointsResponseDTO> response = ApiResponseDTO.success(
                "Contribution points calculated and allocated successfully", responseDTO);

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Get all contribution points records for a specific student.
     *
     * GET /api/contributions/student/{studentId}
     *
     * @param studentId the student ID
     * @return list of contribution points records
     */
    @GetMapping("/student/{studentId}")
    public ResponseEntity<ApiResponseDTO<List<ContributionPointsResponseDTO>>> getPointsByStudent(
            @PathVariable("studentId") Integer studentId) {

        List<ContributionPointsResponseDTO> points =
                contributionPointsService.getPointsByStudent(studentId);

        ApiResponseDTO<List<ContributionPointsResponseDTO>> response = ApiResponseDTO.success(
                "Contribution points retrieved successfully", points);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Get the total contribution points for a specific student.
     *
     * GET /api/contributions/student/{studentId}/total
     *
     * @param studentId the student ID
     * @return the student's total points
     */
    @GetMapping("/student/{studentId}/total")
    public ResponseEntity<ApiResponseDTO<StudentTotalPointsDTO>> getTotalPoints(
            @PathVariable("studentId") Integer studentId) {

        StudentTotalPointsDTO totalPoints = contributionPointsService.getTotalPoints(studentId);

        ApiResponseDTO<StudentTotalPointsDTO> response = ApiResponseDTO.success(
                "Total contribution points retrieved successfully", totalPoints);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
