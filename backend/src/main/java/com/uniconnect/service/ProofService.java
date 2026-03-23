package com.uniconnect.service;

import com.uniconnect.dto.ProofSubmissionRequest;
import com.uniconnect.dto.ProofSubmissionResponse;
import com.uniconnect.dto.ProofReviewRequest;
import com.uniconnect.dto.PointRecordResponse;
import com.uniconnect.dto.ReviewPointsRequest;
import com.uniconnect.dto.AllocatePointsRequest;
import com.uniconnect.model.PointRecord;
import com.uniconnect.model.PointStatus;
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
    private final PointService pointService;
    private final SystemNotificationService systemNotificationService;

    public ProofService(ProofSubmissionRepository proofRepository, UserRepository userRepository,
                        PointRecordRepository pointRecordRepository,
                        PointService pointService,
                        SystemNotificationService systemNotificationService) {
        this.proofRepository = proofRepository;
        this.userRepository = userRepository;
        this.pointRecordRepository = pointRecordRepository;
        this.pointService = pointService;
        this.systemNotificationService = systemNotificationService;
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

    public ProofSubmissionResponse reviewProof(User reviewer, Long proofId, ProofReviewRequest request) {
        if (reviewer.getRole() != Role.DEPARTMENT_HEAD && reviewer.getRole() != Role.DEPARTMENT_ASSISTANT) {
            throw new IllegalArgumentException("Only department heads or assistants can review proof submissions.");
        }

        ProofSubmission proof = getProofById(proofId);
        String action = request.getAction() == null ? "" : request.getAction().trim().toUpperCase();
        if (!action.equals("APPROVE") && !action.equals("REJECT")) {
            throw new IllegalArgumentException("Action must be APPROVE or REJECT.");
        }

        List<PointRecord> proofRecords = pointRecordRepository.findByProofSubmissionId(proofId);
        PointRecord existingRecord = proofRecords.stream()
                .max(Comparator.comparing(PointRecord::getAllocatedAt, Comparator.nullsLast(Comparator.naturalOrder())))
                .orElse(null);

        if (action.equals("APPROVE")) {
            if (request.getPoints() == null || request.getPoints() <= 0) {
                throw new IllegalArgumentException("Approved proof submissions must include points.");
            }
            if (request.getCategory() == null) {
                request.setCategory(proof.getLatestCategory());
            }
            if (request.getCategory() == null) {
                throw new IllegalArgumentException("This submission does not have a category. Ask the student to resubmit it.");
            }

            approveProofWithPoints(reviewer, proof, existingRecord, request);
            return toResponse(proofRepository.findById(proof.getId()).orElse(proof));
        }

        if (existingRecord != null && existingRecord.getStatus() == PointStatus.PENDING) {
            ReviewPointsRequest reviewRequest = new ReviewPointsRequest();
            reviewRequest.setAction("REJECT");
            reviewRequest.setNote(request.getNote());
            pointService.reviewPoints(reviewer, existingRecord.getId(), reviewRequest);
        } else {
            proof.setLatestStatus(PointStatus.REJECTED);
            proof.setLatestPoints(0);
            proofRepository.save(proof);
            systemNotificationService.createNotification(
                    proof.getStudent().getId(),
                    "Evidence Rejected",
                    "Your evidence submission \"" + proof.getTitle() + "\" was rejected by the department.",
                    "/undergraduate/points"
            );
        }

        return toResponse(proofRepository.findById(proof.getId()).orElse(proof));
    }

    private void approveProofWithPoints(User reviewer, ProofSubmission proof, PointRecord existingRecord,
                                        ProofReviewRequest request) {
        if (existingRecord == null) {
            AllocatePointsRequest allocateRequest = new AllocatePointsRequest();
            allocateRequest.setStudentId(proof.getStudent().getId());
            allocateRequest.setProofId(proof.getId());
            allocateRequest.setPoints(request.getPoints());
            allocateRequest.setCategory(request.getCategory());
            allocateRequest.setNote(request.getNote());
            PointRecordResponse createdRecord = pointService.allocatePoints(reviewer, allocateRequest);
            existingRecord = pointRecordRepository.findById(createdRecord.getId())
                    .orElseThrow(() -> new IllegalArgumentException("Point record was not created successfully."));
        }

        ReviewPointsRequest reviewRequest = new ReviewPointsRequest();
        if (existingRecord.getPoints() != null && existingRecord.getPoints().equals(request.getPoints())
                && existingRecord.getCategory() == request.getCategory()) {
            reviewRequest.setAction("APPROVE");
        } else {
            reviewRequest.setAction("ADJUST");
            reviewRequest.setAdjustedPoints(request.getPoints());
            reviewRequest.setCategory(request.getCategory().name());
        }
        reviewRequest.setNote(request.getNote());
        pointService.reviewPoints(reviewer, existingRecord.getId(), reviewRequest);
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
