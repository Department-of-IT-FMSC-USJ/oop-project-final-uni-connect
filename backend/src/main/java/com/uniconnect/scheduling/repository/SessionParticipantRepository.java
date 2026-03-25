package com.uniconnect.scheduling.repository;

import com.uniconnect.scheduling.model.SessionParticipant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SessionParticipantRepository extends JpaRepository<SessionParticipant, Long> {
    List<SessionParticipant> findBySession_Id(Long sessionId);
}


