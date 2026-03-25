package com.uniconnect.academic.modules.allocations.serviceImpl;

import com.uniconnect.academic.modules.allocations.dto.AllocationRequestDTO;
import com.uniconnect.academic.modules.allocations.dto.AllocationResponseDTO;
import com.uniconnect.academic.modules.allocations.entity.StudentAllocation;
import com.uniconnect.academic.modules.allocations.exception.DuplicateAllocationException;
import com.uniconnect.academic.modules.allocations.exception.MentorCapacityExceededException;
import com.uniconnect.academic.modules.allocations.repository.StudentAllocationRepository;
import com.uniconnect.academic.modules.allocations.service.StudentAllocationService;
import com.uniconnect.model.Role;
import com.uniconnect.model.User;
import com.uniconnect.repository.UserRepository;
import com.uniconnect.student.modules.mentor.entity.MentorConnection;
import com.uniconnect.student.modules.mentor.enums.ConnectionStatus;
import com.uniconnect.student.modules.mentor.enums.MentorType;
import com.uniconnect.student.modules.mentor.repository.MentorConnectionRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

/**
 * Implementation of StudentAllocationService.
 * Handles business logic for automatic student allocation to mentors.
 */
@Service
public class StudentAllocationServiceImpl implements StudentAllocationService {

    private final StudentAllocationRepository studentAllocationRepository;
    private final UserRepository userRepository;
    private final MentorConnectionRepository mentorConnectionRepository;

    @Value("${mentor.max-student-capacity:10}")
    private int maxStudentCapacity;

    public StudentAllocationServiceImpl(
            StudentAllocationRepository studentAllocationRepository,
            UserRepository userRepository,
            MentorConnectionRepository mentorConnectionRepository
    ) {
        this.studentAllocationRepository = studentAllocationRepository;
        this.userRepository = userRepository;
        this.mentorConnectionRepository = mentorConnectionRepository;
    }

    /**
     * Allocate a student to a mentor.
     * Checks for duplicate allocation and mentor capacity.
     */
    @Override
    public AllocationResponseDTO allocateStudent(AllocationRequestDTO requestDTO) {

        // Ensure student is not already assigned
        if (studentAllocationRepository.existsByStudentId(requestDTO.getStudentId())) {
            throw new DuplicateAllocationException(
                    "Student with ID " + requestDTO.getStudentId() + " is already allocated to a mentor.");
        }

        // Check mentor capacity
        long currentCount = studentAllocationRepository.countByMentorId(requestDTO.getMentorId());
        if (currentCount >= maxStudentCapacity) {
            throw new MentorCapacityExceededException(
                    "Mentor with ID " + requestDTO.getMentorId() +
                            " has reached the maximum student capacity of " + maxStudentCapacity + ".");
        }

        // Create and save the allocation
        StudentAllocation allocation = new StudentAllocation();
        allocation.setMentorId(requestDTO.getMentorId());
        allocation.setStudentId(requestDTO.getStudentId());
        allocation.setDepartment(requestDTO.getDepartment());

        StudentAllocation savedAllocation = studentAllocationRepository.save(allocation);

        return mapToResponseDTO(savedAllocation);
    }

    /**
     * Automatically allocate an undergraduate to an academic mentor using:
     * - least-loaded strategy (by existing allocations count)
     * - random tie-break among equally-least-loaded mentors
     * - capacity constraint
     */
    @Override
    public AllocationResponseDTO allocateStudentAuto(Integer studentId) {
        if (studentAllocationRepository.existsByStudentId(studentId)) {
            throw new DuplicateAllocationException(
                    "Student with ID " + studentId + " is already allocated to a mentor.");
        }

        User student = userRepository.findById(studentId.longValue())
                .orElseThrow(() -> new IllegalArgumentException("Undergraduate user not found with id: " + studentId));

        if (student.getDepartment() == null || student.getDepartment().trim().isEmpty()) {
            throw new IllegalArgumentException("Undergraduate department is required for auto allocation.");
        }

        // Academic mentors are stored as users with role ACADEMIC_MENTOR.
        List<User> academicMentors = userRepository.findByRole(Role.ACADEMIC_MENTOR);
        if (academicMentors == null || academicMentors.isEmpty()) {
            throw new MentorCapacityExceededException("No academic mentors available for allocation.");
        }

        // Prefer mentors from the same department, but fallback to all if none match.
        String studentDept = student.getDepartment().trim();
        List<User> departmentMatches = academicMentors.stream()
                .filter(m -> m.getDepartment() != null && m.getDepartment().equalsIgnoreCase(studentDept))
                .collect(Collectors.toList());

        List<User> candidateMentors = !departmentMatches.isEmpty() ? departmentMatches : academicMentors;

        // Filter candidates by capacity and compute min allocation count.
        long minCount = Long.MAX_VALUE;
        List<User> eligibleMentors = new ArrayList<>();
        for (User mentor : candidateMentors) {
            int mentorId = mentor.getId().intValue();
            long currentCount = studentAllocationRepository.countByMentorId(mentorId);
            if (currentCount >= maxStudentCapacity) {
                continue;
            }
            if (currentCount < minCount) {
                minCount = currentCount;
                eligibleMentors.clear();
                eligibleMentors.add(mentor);
            } else if (currentCount == minCount) {
                eligibleMentors.add(mentor);
            }
        }

        if (eligibleMentors.isEmpty()) {
            throw new MentorCapacityExceededException(
                    "All academic mentors have reached the maximum student capacity of " + maxStudentCapacity + ".");
        }

        // Random tie-break among equally least-loaded mentors.
        User chosenMentor = eligibleMentors.get(ThreadLocalRandom.current().nextInt(eligibleMentors.size()));

        StudentAllocation allocation = new StudentAllocation();
        allocation.setMentorId(chosenMentor.getId().intValue());
        allocation.setStudentId(studentId);
        allocation.setDepartment(studentDept);

        StudentAllocation savedAllocation = studentAllocationRepository.save(allocation);

        // Create approved academic mentor connection for feedback/session linkage.
        int chosenMentorId = chosenMentor.getId().intValue();
        if (!mentorConnectionRepository.existsByStudentIdAndMentorId(studentId, chosenMentorId)) {
            MentorConnection connection = new MentorConnection();
            connection.setStudentId(studentId);
            connection.setMentorId(chosenMentorId);
            connection.setMentorType(MentorType.Academic);
            connection.setConnectionStatus(ConnectionStatus.Approved);
            mentorConnectionRepository.save(connection);
        }

        return mapToResponseDTO(savedAllocation);
    }

    /**
     * Get all student allocations for a specific mentor.
     */
    @Override
    public List<AllocationResponseDTO> getAllocationsByMentor(Integer mentorId) {
        List<StudentAllocation> allocations =
                studentAllocationRepository.findByMentorIdOrderByAllocationDateDesc(mentorId);

        return allocations.stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Map StudentAllocation entity to AllocationResponseDTO.
     */
    private AllocationResponseDTO mapToResponseDTO(StudentAllocation allocation) {
        User student = null;
        try {
            student = userRepository.findById(allocation.getStudentId().longValue()).orElse(null);
        } catch (Exception ignored) {
            // Best-effort mapping (student details are not critical for allocation functionality).
        }

        String studentName = student != null ? student.getFullName() : null;
        Integer academicYear = null;
        if (student != null && student.getYearOfStudy() != null && !student.getYearOfStudy().trim().isEmpty()) {
            try {
                academicYear = Integer.parseInt(student.getYearOfStudy().trim());
            } catch (NumberFormatException ignored) {
                academicYear = null;
            }
        }

        return AllocationResponseDTO.builder()
                .allocationId(allocation.getAllocationId())
                .mentorId(allocation.getMentorId())
                .studentId(allocation.getStudentId())
                .studentName(studentName)
                .department(allocation.getDepartment())
                .academicYear(academicYear)
                .allocationDate(allocation.getAllocationDate())
                .build();
    }
}
