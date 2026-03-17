package com.uniconnect.repository;

import com.uniconnect.model.PointAudit;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PointAuditRepository extends JpaRepository<PointAudit, Long> {
    java.util.List<PointAudit> findByPointRecordId(Long pointRecordId);
}
