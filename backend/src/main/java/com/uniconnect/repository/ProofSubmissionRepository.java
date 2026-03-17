package com.uniconnect.repository;

import com.uniconnect.model.ProofSubmission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProofSubmissionRepository extends JpaRepository<ProofSubmission, Long> {
    List<ProofSubmission> findByStudentId(Long studentId);
}
