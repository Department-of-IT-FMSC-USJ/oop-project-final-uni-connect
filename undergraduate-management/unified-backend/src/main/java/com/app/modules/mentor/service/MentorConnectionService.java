package com.app.modules.mentor.service;

import com.app.modules.mentor.dto.MentorConnectionRequestDTO;
import com.app.modules.mentor.dto.MentorConnectionResponseDTO;

import java.util.List;

/**
 * Service interface for Mentor Connection operations.
 */
public interface MentorConnectionService {

    /**
     * Request a mentor connection.
     * Academic mentors are available to all students.
     * Industry mentors require minimum contribution points.
     *
     * @param requestDTO the mentor connection request details
     * @return the created mentor connection details
     */
    MentorConnectionResponseDTO requestConnection(MentorConnectionRequestDTO requestDTO);

    /**
     * Get all mentor connections for a specific student.
     *
     * @param studentId the student ID
     * @return list of mentor connections
     */
    List<MentorConnectionResponseDTO> getConnectionsByStudent(Integer studentId);
}
