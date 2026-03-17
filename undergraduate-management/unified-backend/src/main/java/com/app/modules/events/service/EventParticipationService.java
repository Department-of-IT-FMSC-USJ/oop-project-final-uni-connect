package com.app.modules.events.service;

import com.app.modules.events.dto.EventParticipationRequestDTO;
import com.app.modules.events.dto.EventParticipationResponseDTO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Service interface for Event Participation operations.
 */
public interface EventParticipationService {

    /**
     * Submit a new event participation.
     *
     * @param requestDTO the event participation details
     * @param file       the evidence PDF file
     * @param studentId  the ID of the student submitting
     * @param userRole   the role of the user
     * @return the submitted event participation details
     */
    EventParticipationResponseDTO submitParticipation(EventParticipationRequestDTO requestDTO,
                                                       MultipartFile file,
                                                       Integer studentId,
                                                       String userRole);

    /**
     * Get all event participations for a specific student.
     *
     * @param studentId the student ID
     * @return list of event participations
     */
    List<EventParticipationResponseDTO> getParticipationsByStudent(Integer studentId);
}
