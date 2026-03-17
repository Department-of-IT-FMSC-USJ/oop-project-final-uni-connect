package com.uniconnect.service;

import com.uniconnect.dto.AllocatePointsRequest;
import com.uniconnect.dto.EligibleStudentResponse;
import com.uniconnect.dto.PointAuditResponse;
import com.uniconnect.dto.PointRecordResponse;
import com.uniconnect.dto.ProofSummary;
import com.uniconnect.dto.ReviewPointsRequest;
import com.uniconnect.model.*;
import com.uniconnect.repository.PointAuditRepository;
import com.uniconnect.repository.PointRecordRepository;
import com.uniconnect.repository.ProofSubmissionRepository;
import com.uniconnect.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PointService {

    private final PointRecordRepository pointRecordRepository;
    private final PointAuditRepository pointAuditRepository;
    private final ProofSubmissionRepository proofRepository;
    private final UserRepository userRepository;

    private final int mentorThreshold;

    public PointService(PointRecordRepository pointRecordRepository,
                        PointAuditRepository pointAuditRepository,
                        ProofSubmissionRepository proofRepository,
                        UserRepository userRepository,
                        @Value("${uniconnect.points.mentor-threshold:50}") int mentorThreshold) {
        this.pointRecordRepository = pointRecordRepository;
        this.pointAuditRepository = pointAuditRepository;
        this.proofRepository = proofRepository;
        this.userRepository = userRepository;
        this.mentorThreshold = mentorThreshold;
    }

    public PointRecordResponse allocatePoints(User rep, AllocatePointsRequest request) {
        requireRole(rep, Role.DEPARTMENT_REP, "Only department representatives can allocate points.");

        User student = userRepository.findById(request.getStudentId())
                .orElseThrow(() -> new IllegalArgumentException("Student not found"));
        if (student.getRole() != Role.UNDERGRADUATE) {
            throw new IllegalArgumentException("Points can only be allocated to undergraduates.");
        }

        ProofSubmission proof = null;
        if (request.getProofId() != null) {
            proof = proofRepository.findById(request.getProofId())
                    .orElseThrow(() -> new IllegalArgumentException("Proof submission not found"));
            if (!proof.getStudent().getId().equals(student.getId())) {
                throw new IllegalArgumentException("Proof submission does not belong to the student.");
            }
        }

        validatePoints(request.getCategory(), request.getPoints());

        PointRecord record = new PointRecord(
                student,
                proof,
                rep,
                request.getCategory(),
                PointStatus.PENDING,
                request.getPoints(),
                request.getPoints(),
                request.getNote(),
                LocalDateTime.now()
        );

        PointRecord saved = pointRecordRepository.save(record);
        pointAuditRepository.save(new PointAudit(saved, rep, PointAction.CREATED, null, saved.getPoints(),
                request.getNote(), LocalDateTime.now()));
        if (proof != null) {
            proof.setLatestPoints(saved.getPoints());
            proof.setLatestStatus(saved.getStatus());
            proof.setLatestCategory(saved.getCategory());
            proofRepository.save(proof);
        }

        return toResponse(saved);
    }

    public List<PointRecordResponse> getPendingPoints(User head) {
        requireRole(head, Role.DEPARTMENT_HEAD, "Only department heads can review points.");
        return pointRecordRepository.findByStatus(PointStatus.PENDING).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<PointRecordResponse> getPointsForStudent(User student) {
        return pointRecordRepository.findByStudentId(student.getId()).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<PointAuditResponse> getAuditForRecord(User requester, Long recordId) {
        PointRecord record = pointRecordRepository.findById(recordId)
                .orElseThrow(() -> new IllegalArgumentException("Point record not found"));

        if (requester.getRole() == Role.UNDERGRADUATE) {
            if (!record.getStudent().getId().equals(requester.getId())) {
                throw new IllegalArgumentException("You can only view your own audit records.");
            }
        } else if (requester.getRole() != Role.DEPARTMENT_REP && requester.getRole() != Role.DEPARTMENT_HEAD) {
            throw new IllegalArgumentException("Not authorized to view audit records.");
        }

        return pointAuditRepository.findByPointRecordId(recordId).stream()
                .map(this::toAuditResponse)
                .collect(Collectors.toList());
    }

    public PointRecordResponse reviewPoints(User head, Long recordId, ReviewPointsRequest request) {
        requireRole(head, Role.DEPARTMENT_HEAD, "Only department heads can review points.");

        PointRecord record = pointRecordRepository.findById(recordId)
                .orElseThrow(() -> new IllegalArgumentException("Point record not found"));

        PointStatus beforeStatus = record.getStatus();
        int beforePoints = Optional.ofNullable(record.getPoints()).orElse(0);

        PointAction action = parseAction(request.getAction());
        switch (action) {
            case APPROVED -> record.setStatus(PointStatus.APPROVED);
            case REJECTED -> record.setStatus(PointStatus.REJECTED);
            case ADJUSTED -> {
                if (request.getAdjustedPoints() == null) {
                    throw new IllegalArgumentException("Adjusted points are required for ADJUSTED action.");
                }
                PointCategory nextCategory = record.getCategory();
                if (request.getCategory() != null && !request.getCategory().isBlank()) {
                    nextCategory = PointCategory.valueOf(request.getCategory().trim().toUpperCase());
                }
                validatePoints(nextCategory, request.getAdjustedPoints());
                record.setCategory(nextCategory);
                record.setPoints(request.getAdjustedPoints());
                record.setStatus(PointStatus.APPROVED);
            }
            default -> throw new IllegalArgumentException("Unsupported action.");
        }

        record.setReviewedBy(head);
        record.setReviewedAt(LocalDateTime.now());
        record.setReviewNote(request.getNote());

        PointRecord saved = pointRecordRepository.save(record);

        int afterPoints = Optional.ofNullable(saved.getPoints()).orElse(0);
        applyPointsDelta(saved.getStudent(), beforeStatus, saved.getStatus(), beforePoints, afterPoints);

        pointAuditRepository.save(new PointAudit(saved, head, action, beforePoints, afterPoints,
                request.getNote(), LocalDateTime.now()));
        if (saved.getProofSubmission() != null) {
            ProofSubmission proof = saved.getProofSubmission();
            proof.setLatestPoints(saved.getPoints());
            proof.setLatestStatus(saved.getStatus());
            proof.setLatestCategory(saved.getCategory());
            proofRepository.save(proof);
        }

        return toResponse(saved);
    }

    public List<EligibleStudentResponse> getEligibleStudents(User requester) {
        if (requester.getRole() != Role.DEPARTMENT_REP && requester.getRole() != Role.DEPARTMENT_HEAD) {
            throw new IllegalArgumentException("Only department reps or heads can view eligible students.");
        }

        List<User> eligible = userRepository.findByMentorEligibleTrue();
        return eligible.stream().map(this::buildEligibleResponse).collect(Collectors.toList());
    }

    private EligibleStudentResponse buildEligibleResponse(User student) {
        List<ProofSubmission> proofs = proofRepository.findByStudentId(student.getId());
        Map<Long, PointRecord> latestByProof = pointRecordRepository.findByStudentId(student.getId()).stream()
                .filter(record -> record.getProofSubmission() != null)
                .collect(Collectors.groupingBy(record -> record.getProofSubmission().getId(),
                        Collectors.collectingAndThen(
                                Collectors.maxBy(Comparator.comparing(PointRecord::getAllocatedAt)),
                                opt -> opt.orElse(null)
                        )));

        List<ProofSummary> proofSummaries = proofs.stream()
                .map(proof -> {
                    PointRecord record = latestByProof.get(proof.getId());
                    return new ProofSummary(
                            proof.getId(),
                            proof.getTitle(),
                            proof.getEventDate(),
                            proof.getProofType(),
                            proof.getCreatedAt(),
                            record == null ? null : record.getPoints(),
                            record == null ? null : record.getStatus()
                    );
                })
                .collect(Collectors.toList());

        return new EligibleStudentResponse(
                student.getId(),
                student.getFullName(),
                student.getDepartment(),
                Optional.ofNullable(student.getCumulativePoints()).orElse(0),
                Optional.ofNullable(student.getMentorEligible()).orElse(false),
                proofSummaries
        );
    }

    private PointRecordResponse toResponse(PointRecord record) {
        return new PointRecordResponse(
                record.getId(),
                record.getStudent().getId(),
                record.getStudent().getFullName(),
                record.getProofSubmission() == null ? null : record.getProofSubmission().getId(),
                record.getOriginalPoints(),
                record.getPoints(),
                record.getCategory(),
                record.getStatus(),
                record.getNote(),
                record.getReviewNote(),
                record.getAllocatedBy() == null ? null : record.getAllocatedBy().getFullName(),
                record.getReviewedBy() == null ? null : record.getReviewedBy().getFullName(),
                record.getAllocatedAt(),
                record.getReviewedAt()
        );
    }

    private PointAuditResponse toAuditResponse(PointAudit audit) {
        return new PointAuditResponse(
                audit.getId(),
                audit.getPointRecord().getId(),
                audit.getAction(),
                audit.getActor() == null ? null : audit.getActor().getFullName(),
                audit.getBeforePoints(),
                audit.getAfterPoints(),
                audit.getNote(),
                audit.getCreatedAt()
        );
    }

    private void applyPointsDelta(User student, PointStatus beforeStatus, PointStatus afterStatus,
                                  int beforePoints, int afterPoints) {
        int current = Optional.ofNullable(student.getCumulativePoints()).orElse(0);
        int delta = 0;

        if (beforeStatus != PointStatus.APPROVED && afterStatus == PointStatus.APPROVED) {
            delta = afterPoints;
        } else if (beforeStatus == PointStatus.APPROVED && afterStatus == PointStatus.APPROVED) {
            delta = afterPoints - beforePoints;
        } else if (beforeStatus == PointStatus.APPROVED && afterStatus != PointStatus.APPROVED) {
            delta = -beforePoints;
        }

        if (delta != 0) {
            student.setCumulativePoints(current + delta);
            student.setMentorEligible((current + delta) >= mentorThreshold);
            userRepository.save(student);
        } else if (student.getMentorEligible() == null) {
            student.setMentorEligible(current >= mentorThreshold);
            userRepository.save(student);
        }
    }

    private void validatePoints(PointCategory category, Integer points) {
        if (points == null || points <= 0) {
            throw new IllegalArgumentException("Points must be greater than zero.");
        }

        int max = switch (category) {
            case ACTIVITY, DIRECT -> 5;
            case AWARD -> 25;
        };

        if (points > max) {
            throw new IllegalArgumentException("Points exceed allowed range for " + category);
        }
    }

    private void requireRole(User user, Role role, String message) {
        if (user.getRole() != role) {
            throw new IllegalArgumentException(message);
        }
    }

    private PointAction parseAction(String action) {
        if (action == null) {
            throw new IllegalArgumentException("Action is required.");
        }
        String normalized = action.trim().toUpperCase();
        return switch (normalized) {
            case "APPROVE", "APPROVED" -> PointAction.APPROVED;
            case "REJECT", "REJECTED" -> PointAction.REJECTED;
            case "ADJUST", "ADJUSTED" -> PointAction.ADJUSTED;
            default -> throw new IllegalArgumentException("Invalid action. Use APPROVE, REJECT, or ADJUST.");
        };
    }
}
