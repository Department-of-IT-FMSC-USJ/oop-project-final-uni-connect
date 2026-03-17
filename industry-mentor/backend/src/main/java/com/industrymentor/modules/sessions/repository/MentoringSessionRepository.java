package com.industrymentor.modules.sessions.repository;

import com.industrymentor.modules.sessions.entity.MentoringSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface MentoringSessionRepository extends JpaRepository<MentoringSession, Integer> {

    List<MentoringSession> findByMentorIdOrderBySessionDateDescSessionTimeDesc(Integer mentorId);

    boolean existsByMentorIdAndSessionDateAndSessionTime(Integer mentorId, LocalDate sessionDate, LocalTime sessionTime);
}
