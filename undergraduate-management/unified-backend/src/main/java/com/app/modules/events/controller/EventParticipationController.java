package com.app.modules.events.controller;

import com.app.common.dto.ApiResponseDTO;
import com.app.modules.events.dto.EventParticipationRequestDTO;
import com.app.modules.events.dto.EventParticipationResponseDTO;
import com.app.modules.events.service.EventParticipationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * REST Controller for Event Participation operations.
 * Provides endpoints for submitting and retrieving event participations.
 */
@RestController
@RequestMapping("/api/events")
public class EventParticipationController {

    private final EventParticipationService eventParticipationService;

    public EventParticipationController(EventParticipationService eventParticipationService) {
        this.eventParticipationService = eventParticipationService;
    }

    /**
     * Submit a new event participation.
     *
     * POST /api/events/participation
     *
     * @param requestDTO the event participation details
     * @param file       the evidence PDF file
     * @param studentId  the ID of the student (passed via header)
     * @param userRole   the role of the user (passed via header)
     * @return the submitted participation details
     */
    @PostMapping("/participation")
    public ResponseEntity<ApiResponseDTO<EventParticipationResponseDTO>> submitParticipation(
            @Valid @RequestPart("participation") EventParticipationRequestDTO requestDTO,
            @RequestPart("file") MultipartFile file,
            @RequestHeader("X-User-Id") Integer studentId,
            @RequestHeader("X-User-Role") String userRole) {

        EventParticipationResponseDTO responseDTO = eventParticipationService.submitParticipation(
                requestDTO, file, studentId, userRole);

        ApiResponseDTO<EventParticipationResponseDTO> response = ApiResponseDTO.success(
                "Event participation submitted successfully", responseDTO);

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Get participation history for a specific student.
     *
     * GET /api/events/participation/student/{studentId}
     *
     * @param studentId the student ID
     * @return list of event participations
     */
    @GetMapping("/participation/student/{studentId}")
    public ResponseEntity<ApiResponseDTO<List<EventParticipationResponseDTO>>> getParticipationsByStudent(
            @PathVariable("studentId") Integer studentId) {

        List<EventParticipationResponseDTO> participations =
                eventParticipationService.getParticipationsByStudent(studentId);

        ApiResponseDTO<List<EventParticipationResponseDTO>> response = ApiResponseDTO.success(
                "Participation history retrieved successfully", participations);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
