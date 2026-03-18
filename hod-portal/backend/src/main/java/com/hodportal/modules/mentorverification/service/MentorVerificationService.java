package com.hodportal.modules.mentorverification.service;

import com.hodportal.modules.mentorverification.dto.VerificationRequestDTO;
import com.hodportal.modules.mentorverification.dto.VerificationResponseDTO;

import java.util.List;

/**
 * Service interface for Industry Mentor Verification operations.
 */
public interface MentorVerificationService {

    /**
     * Get all industry mentor verification records.
     *
     * @return list of verification records
     */
    List<VerificationResponseDTO> getAllMentorVerifications();

    /**
     * Update the verification status of an industry mentor.
     *
     * @param mentorId the mentor ID
     * @param hodId the HoD user ID performing the verification
     * @param requestDTO the verification status update
     * @return the updated verification record
     */
    VerificationResponseDTO updateVerificationStatus(Integer mentorId, Integer hodId, VerificationRequestDTO requestDTO);
}
