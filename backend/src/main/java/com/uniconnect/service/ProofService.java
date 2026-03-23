package com.uniconnect.service;

import com.uniconnect.dto.ProofSubmissionRequest;
import com.uniconnect.dto.ProofSubmissionResponse;
import com.uniconnect.model.PointRecord;
import com.uniconnect.model.ProofSubmission;
import com.uniconnect.model.Role;
import com.uniconnect.model.User;
import com.uniconnect.repository.PointRecordRepository;
import com.uniconnect.repository.ProofSubmissionRepository;
import com.uniconnect.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ProofService {
    private static final String GOOGLE_DRIVE_LINK_REGEX =
            "^(https://)?drive\\.google\\.com/.+$";

    private final ProofSubmissionRepository proofRepository;
    private final UserRepository userRepository;
    private final PointRecordRepository pointRecordRepository;

    public ProofService(ProofSubmissionRepository proofRepository, UserRepository userRepository,
                        PointRecordRepository pointRecordRepository) {
        this.proofRepository = proofRepository;
        this.userRepository = userRepository;
        this.pointRecordRepository = pointRecordRepository;
    }

    public ProofSubmissionResponse submitProof(User student, ProofSubmissionRequest request) {
        if (student.getRole() != Role.UNDERGRADUATE) {
            throw new IllegalArgumentException("Only undergraduates can submit proofs.");
        }
        validateProofRequest(student, request);

        ProofSubmission proof = new ProofSubmission(
                student,
                request.getTitle().trim(),
                request.getDescription() == null ? null : request.getDescription().trim(),
                request.getEventDate(),
                request.getProofData().trim(),
                "GOOGLE_DRIVE",
                LocalDateTime.now()
        );
        proof.setCpm(resolveStudentReference(student));
        proof.setLatestCategory(request.getCategory());
        proof.setSubmissionCode(generateSubmissionCode(proof.getCreatedAt()));

        ProofSubmission saved = proofRepository.save(proof);
        return toResponse(saved);
    }

    private void validateProofRequest(User student, ProofSubmissionRequest request) {
        if (request.getProofData() == null || request.getProofData().trim().isEmpty()) {
            throw new IllegalArgumentException("Evidence link is required.");
        }

        String proofLink = request.getProofData().trim();
        if (!isGoogleDriveLink(proofLink)) {
            throw new IllegalArgumentException("Only Google Drive evidence links are allowed.");
        }

        if (request.getEventDate() == null) {
            throw new IllegalArgumentException("Event date is required.");
        }

        LocalDate today = LocalDate.now();
        LocalDate oldestAllowed = today.minusYears(2);
        if (request.getEventDate().isAfter(today)) {
            throw new IllegalArgumentException("Event date cannot be in the future.");
        }
        if (request.getEventDate().isBefore(oldestAllowed)) {
            throw new IllegalArgumentException("Event date must be within the past two years.");
        }

        if (resolveStudentReference(student) == null) {
            throw new IllegalArgumentException("Student registration details are required before submitting evidence.");
        }
    }

    private String resolveStudentReference(User student) {
        if (student.getRegistrationNumber() != null && !student.getRegistrationNumber().trim().isEmpty()) {
            return student.getRegistrationNumber().trim();
        }
        if (student.getCpmNumber() != null && !student.getCpmNumber().trim().isEmpty()) {
            return student.getCpmNumber().trim();
        }
        return null;
    }

    private boolean isGoogleDriveLink(String proofLink) {
        if (!proofLink.matches(GOOGLE_DRIVE_LINK_REGEX)) {
            return false;
        }

        try {
            URI uri = new URI(proofLink.startsWith("http") ? proofLink : "https://" + proofLink);
            String host = uri.getHost();
            return host != null && host.equalsIgnoreCase("drive.google.com");
        } catch (URISyntaxException ignored) {
            return false;
        }
    }

    public List<ProofSubmissionResponse> getProofsForStudent(User student) {
        return proofRepository.findByStudentId(student.getId()).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<ProofSubmissionResponse> getProofsForReps(Optional<Long> studentId) {
        List<ProofSubmission> proofs = studentId
                .map(proofRepository::findByStudentId)
                .orElseGet(proofRepository::findAll);
        return proofs.stream().map(this::toResponse).collect(Collectors.toList());
    }

    public ProofSubmission getProofById(Long id) {
        return proofRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Proof submission not found"));
    }

    public User getStudentById(Long studentId) {
        return userRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found"));
    }

    private ProofSubmissionResponse toResponse(ProofSubmission proof) {
        Optional<PointRecord> latestRecord = pointRecordRepository.findByProofSubmissionId(proof.getId()).stream()
                .max(Comparator.comparing(PointRecord::getAllocatedAt));
        Integer latestPoints = latestRecord.map(PointRecord::getPoints).orElse(proof.getLatestPoints());
        String latestStatus = latestRecord
                .map(record -> record.getStatus().name())
                .orElse(proof.getLatestStatus() == null ? null : proof.getLatestStatus().name());
        String latestCategory = latestRecord
                .map(record -> record.getCategory().name())
                .orElse(proof.getLatestCategory() == null ? null : proof.getLatestCategory().name());
        Long latestRecordId = latestRecord.map(PointRecord::getId).orElse(null);
        String latestAllocatedByName = latestRecord
                .map(record -> record.getAllocatedBy() == null ? null : record.getAllocatedBy().getFullName())
                .orElse(null);
        return new ProofSubmissionResponse(
                proof.getId(),
                proof.getStudent().getId(),
                proof.getStudent().getFullName(),
                proof.getTitle(),
                proof.getDescription(),
                proof.getCpm(),
                proof.getSubmissionCode(),
                proof.getEventDate(),
                proof.getProofData(),
                proof.getProofType(),
                proof.getCreatedAt(),
                latestPoints,
                latestStatus,
                latestCategory,
                latestRecordId,
                latestAllocatedByName
        );
    }

    private String generateSubmissionCode(LocalDateTime createdAt) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");
        return "SUB-" + createdAt.format(formatter);
    }
}
