package com.uniconnect.repository;

import com.uniconnect.model.PointRecord;
import com.uniconnect.model.PointStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PointRecordRepository extends JpaRepository<PointRecord, Long> {
    List<PointRecord> findByStatus(PointStatus status);
    List<PointRecord> findByStudentId(Long studentId);
    List<PointRecord> findByProofSubmissionId(Long proofId);
}
