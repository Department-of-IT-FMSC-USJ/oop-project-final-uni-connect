package com.academicmentor.modules.contactinfo.serviceImpl;

import com.academicmentor.common.exception.ResourceNotFoundException;
import com.academicmentor.modules.contactinfo.dto.ContactInfoRequestDTO;
import com.academicmentor.modules.contactinfo.dto.ContactInfoResponseDTO;
import com.academicmentor.modules.contactinfo.entity.MentorContactInfo;
import com.academicmentor.modules.contactinfo.repository.MentorContactInfoRepository;
import com.academicmentor.modules.contactinfo.service.MentorContactInfoService;
import org.springframework.stereotype.Service;

/**
 * Implementation of MentorContactInfoService.
 * Handles business logic for storing and retrieving mentor contact details.
 */
@Service
public class MentorContactInfoServiceImpl implements MentorContactInfoService {

    private final MentorContactInfoRepository mentorContactInfoRepository;

    public MentorContactInfoServiceImpl(MentorContactInfoRepository mentorContactInfoRepository) {
        this.mentorContactInfoRepository = mentorContactInfoRepository;
    }

    /**
     * Save or update mentor contact information.
     */
    @Override
    public ContactInfoResponseDTO saveContactInfo(ContactInfoRequestDTO requestDTO) {

        // Check if contact info already exists (update case)
        MentorContactInfo contactInfo = mentorContactInfoRepository
                .findByMentorId(requestDTO.getMentorId())
                .orElse(new MentorContactInfo());

        contactInfo.setMentorId(requestDTO.getMentorId());
        contactInfo.setMentorName(requestDTO.getMentorName());
        contactInfo.setEmail(requestDTO.getEmail());
        contactInfo.setPhone(requestDTO.getPhone());
        contactInfo.setOfficeLocation(requestDTO.getOfficeLocation());
        contactInfo.setOfficeHours(requestDTO.getOfficeHours());

        MentorContactInfo savedInfo = mentorContactInfoRepository.save(contactInfo);

        return mapToResponseDTO(savedInfo);
    }

    /**
     * Get contact information for a specific mentor.
     */
    @Override
    public ContactInfoResponseDTO getContactInfoByMentor(Integer mentorId) {
        MentorContactInfo contactInfo = mentorContactInfoRepository.findByMentorId(mentorId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Contact information not found for mentor ID: " + mentorId));

        return mapToResponseDTO(contactInfo);
    }

    /**
     * Map MentorContactInfo entity to ContactInfoResponseDTO.
     */
    private ContactInfoResponseDTO mapToResponseDTO(MentorContactInfo contactInfo) {
        return ContactInfoResponseDTO.builder()
                .mentorId(contactInfo.getMentorId())
                .mentorName(contactInfo.getMentorName())
                .email(contactInfo.getEmail())
                .phone(contactInfo.getPhone())
                .officeLocation(contactInfo.getOfficeLocation())
                .officeHours(contactInfo.getOfficeHours())
                .build();
    }
}
