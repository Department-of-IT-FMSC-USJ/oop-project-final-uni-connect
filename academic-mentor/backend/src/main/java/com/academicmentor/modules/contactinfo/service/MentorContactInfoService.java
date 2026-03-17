package com.academicmentor.modules.contactinfo.service;

import com.academicmentor.modules.contactinfo.dto.ContactInfoRequestDTO;
import com.academicmentor.modules.contactinfo.dto.ContactInfoResponseDTO;

/**
 * Service interface for Mentor Contact Information operations.
 */
public interface MentorContactInfoService {

    /**
     * Save or update mentor contact information.
     *
     * @param requestDTO the contact info details
     * @return the saved contact info details
     */
    ContactInfoResponseDTO saveContactInfo(ContactInfoRequestDTO requestDTO);

    /**
     * Get contact information for a specific mentor.
     *
     * @param mentorId the mentor ID
     * @return the contact info details
     */
    ContactInfoResponseDTO getContactInfoByMentor(Integer mentorId);
}
