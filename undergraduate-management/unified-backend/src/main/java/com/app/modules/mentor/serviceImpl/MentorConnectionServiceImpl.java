package com.app.modules.mentor.serviceImpl;

import com.app.modules.mentor.dto.MentorConnectionRequestDTO;
import com.app.modules.mentor.dto.MentorConnectionResponseDTO;
import com.app.modules.mentor.entity.MentorConnection;
import com.app.modules.mentor.enums.ConnectionStatus;
import com.app.modules.mentor.enums.MentorType;
import com.app.modules.mentor.exception.InsufficientPointsException;
import com.app.modules.points.repository.ContributionPointsRepository;
import com.app.modules.mentor.repository.MentorConnectionRepository;
import com.app.modules.mentor.service.MentorConnectionService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of MentorConnectionService.
 * Handles business logic for mentor allocation and connection handling.
 */
@Service
public class MentorConnectionServiceImpl implements MentorConnectionService {

    private final MentorConnectionRepository mentorConnectionRepository;
    private final ContributionPointsRepository contributionPointsRepository;

    @Value("${mentor.industry.min-points}")
    private Integer industryMentorMinPoints;

    public MentorConnectionServiceImpl(MentorConnectionRepository mentorConnectionRepository,
                                        ContributionPointsRepository contributionPointsRepository) {
        this.mentorConnectionRepository = mentorConnectionRepository;
        this.contributionPointsRepository = contributionPointsRepository;
    }

    /**
     * Request a mentor connection.
     * Academic mentors are available to all students.
     * Industry mentors require the student to have minimum contribution points.
     */
    @Override
    public MentorConnectionResponseDTO requestConnection(MentorConnectionRequestDTO requestDTO) {

        // Check if connection already exists between student and mentor
        if (mentorConnectionRepository.existsByStudentIdAndMentorId(
                requestDTO.getStudentId(), requestDTO.getMentorId())) {
            throw new IllegalStateException(
                    "A connection already exists between student " + requestDTO.getStudentId() +
                            " and mentor " + requestDTO.getMentorId());
        }

        // If requesting an Industry mentor, check student's contribution points
        if (requestDTO.getMentorType() == MentorType.Industry) {
            Integer totalPoints = contributionPointsRepository.getTotalPointsByStudentId(
                    requestDTO.getStudentId());

            if (totalPoints < industryMentorMinPoints) {
                throw new InsufficientPointsException(
                        "Insufficient contribution points for Industry mentor connection. " +
                                "Required: " + industryMentorMinPoints +
                                ", Current: " + totalPoints);
            }
        }

        // Create and save the mentor connection
        MentorConnection connection = new MentorConnection();
        connection.setStudentId(requestDTO.getStudentId());
        connection.setMentorId(requestDTO.getMentorId());
        connection.setMentorType(requestDTO.getMentorType());
        connection.setConnectionStatus(ConnectionStatus.Pending);

        MentorConnection savedConnection = mentorConnectionRepository.save(connection);

        return mapToResponseDTO(savedConnection);
    }

    /**
     * Get all mentor connections for a specific student.
     */
    @Override
    public List<MentorConnectionResponseDTO> getConnectionsByStudent(Integer studentId) {
        List<MentorConnection> connections =
                mentorConnectionRepository.findByStudentIdOrderByCreatedDateDesc(studentId);

        return connections.stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Map MentorConnection entity to MentorConnectionResponseDTO.
     *
     * @param connection the entity to map
     * @return the response DTO
     */
    private MentorConnectionResponseDTO mapToResponseDTO(MentorConnection connection) {
        return MentorConnectionResponseDTO.builder()
                .connectionId(connection.getConnectionId())
                .studentId(connection.getStudentId())
                .mentorId(connection.getMentorId())
                .mentorType(connection.getMentorType())
                .connectionStatus(connection.getConnectionStatus())
                .createdDate(connection.getCreatedDate())
                .build();
    }
}
