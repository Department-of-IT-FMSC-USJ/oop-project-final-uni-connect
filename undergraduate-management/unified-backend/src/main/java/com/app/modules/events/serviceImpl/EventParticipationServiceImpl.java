package com.app.modules.events.serviceImpl;

import com.app.modules.events.dto.EventParticipationRequestDTO;
import com.app.modules.events.dto.EventParticipationResponseDTO;
import com.app.modules.events.entity.EventParticipation;
import com.app.modules.events.enums.VerificationStatus;
import com.app.modules.events.exception.DuplicateSubmissionException;
import com.app.common.exception.UnauthorizedAccessException;
import com.app.modules.events.repository.EventParticipationRepository;
import com.app.modules.events.service.EventParticipationService;
import com.app.modules.events.util.EventFileStorageUtil;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of EventParticipationService.
 * Handles business logic for event participation operations.
 */
@Service
public class EventParticipationServiceImpl implements EventParticipationService {

    private final EventParticipationRepository eventParticipationRepository;
    private final EventFileStorageUtil fileStorageUtil;

    public EventParticipationServiceImpl(EventParticipationRepository eventParticipationRepository,
                                          EventFileStorageUtil fileStorageUtil) {
        this.eventParticipationRepository = eventParticipationRepository;
        this.fileStorageUtil = fileStorageUtil;
    }

    /**
     * Submit a new event participation.
     * Validates user authentication, checks for duplicates, stores evidence file.
     */
    @Override
    public EventParticipationResponseDTO submitParticipation(EventParticipationRequestDTO requestDTO,
                                                              MultipartFile file,
                                                              Integer studentId,
                                                              String userRole) {

        // Validate user authentication
        if (studentId == null || userRole == null || userRole.isBlank()) {
            throw new UnauthorizedAccessException("User must be authenticated to submit event participation.");
        }

        // Check for duplicate event submission by the same student
        if (eventParticipationRepository.existsByStudentIdAndEventName(studentId, requestDTO.getEventName())) {
            throw new DuplicateSubmissionException(
                    "You have already submitted participation for event: " + requestDTO.getEventName());
        }

        // Store the evidence file and get the file URL
        String fileUrl = fileStorageUtil.storeFile(file);

        // Create and save the entity
        EventParticipation participation = new EventParticipation();
        participation.setStudentId(studentId);
        participation.setEventName(requestDTO.getEventName());
        participation.setEventType(requestDTO.getEventType());
        participation.setParticipationRole(requestDTO.getParticipationRole());
        participation.setAchievementDescription(requestDTO.getAchievementDescription());
        participation.setEvidenceFile(fileUrl);
        participation.setVerificationStatus(VerificationStatus.Pending);

        EventParticipation savedParticipation = eventParticipationRepository.save(participation);

        return mapToResponseDTO(savedParticipation);
    }

    /**
     * Get all event participations for a specific student ordered by date (newest first).
     */
    @Override
    public List<EventParticipationResponseDTO> getParticipationsByStudent(Integer studentId) {
        List<EventParticipation> participations =
                eventParticipationRepository.findByStudentIdOrderBySubmittedDateDesc(studentId);

        return participations.stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Map EventParticipation entity to EventParticipationResponseDTO.
     *
     * @param participation the entity to map
     * @return the response DTO
     */
    private EventParticipationResponseDTO mapToResponseDTO(EventParticipation participation) {
        return EventParticipationResponseDTO.builder()
                .participationId(participation.getParticipationId())
                .studentId(participation.getStudentId())
                .eventName(participation.getEventName())
                .eventType(participation.getEventType())
                .participationRole(participation.getParticipationRole())
                .achievementDescription(participation.getAchievementDescription())
                .evidenceFile(participation.getEvidenceFile())
                .verificationStatus(participation.getVerificationStatus())
                .submittedDate(participation.getSubmittedDate())
                .build();
    }
}
