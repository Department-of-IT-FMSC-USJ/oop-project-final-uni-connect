package com.hodportal.modules.mentorverification.serviceImpl;

import com.hodportal.common.exception.ResourceNotFoundException;
import com.hodportal.modules.mentorverification.dto.VerificationRequestDTO;
import com.hodportal.modules.mentorverification.dto.VerificationResponseDTO;
import com.hodportal.modules.mentorverification.entity.IndustryMentorVerification;
import com.hodportal.modules.mentorverification.enums.VerificationStatus;
import com.hodportal.modules.mentorverification.repository.IndustryMentorVerificationRepository;
import com.hodportal.modules.mentorverification.service.MentorVerificationService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of MentorVerificationService.
 * Handles business logic for industry mentor verification.
 */
@Service
public class MentorVerificationServiceImpl implements MentorVerificationService {

    private final IndustryMentorVerificationRepository verificationRepository;

    public MentorVerificationServiceImpl(IndustryMentorVerificationRepository verificationRepository) {
        this.verificationRepository = verificationRepository;
    }

    /**
     * Get all industry mentor verification records.
     */
    @Override
    public List<VerificationResponseDTO> getAllMentorVerifications() {
        List<IndustryMentorVerification> verifications = verificationRepository.findAllByOrderByCreatedAtDesc();

        return verifications.stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Update the verification status of an industry mentor.
     * Only HoD users can perform this action.
     */
    @Override
    public VerificationResponseDTO updateVerificationStatus(Integer mentorId, Integer hodId,
                                                             VerificationRequestDTO requestDTO) {
        IndustryMentorVerification verification = verificationRepository.findById(mentorId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Industry mentor not found with ID: " + mentorId));

        // Parse verification status
        VerificationStatus status;
        try {
            status = VerificationStatus.valueOf(requestDTO.getVerificationStatus().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid verification status: " + requestDTO.getVerificationStatus() +
                    ". Must be PENDING, VERIFIED, or REJECTED.");
        }

        verification.setVerificationStatus(status);
        verification.setHodId(hodId);
        verification.setVerificationDate(LocalDateTime.now());

        IndustryMentorVerification saved = verificationRepository.save(verification);

        return mapToResponseDTO(saved);
    }

    /**
     * Map entity to response DTO.
     */
    private VerificationResponseDTO mapToResponseDTO(IndustryMentorVerification verification) {
        return VerificationResponseDTO.builder()
                .mentorId(verification.getMentorId())
                .mentorName(verification.getMentorName())
                .organization(verification.getOrganization())
                .professionalExpertise(verification.getProfessionalExpertise())
                .contactEmail(verification.getContactEmail())
                .hodId(verification.getHodId())
                .verificationStatus(verification.getVerificationStatus().name())
                .verificationDate(verification.getVerificationDate())
                .createdAt(verification.getCreatedAt())
                .build();
    }
}
