package com.uniconnect.industry.modules.sessions.repository;

import com.uniconnect.industry.modules.sessions.entity.MentoringSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Repository("industryMentoringSessionRepository")
public interface MentoringSessionRepository extends JpaRepository<MentoringSession, Integer> {

    List<MentoringSession> findByMentorIdOrderBySessionDateDescSessionTimeDesc(Integer mentorId);

    boolean existsByMentorIdAndSessionDateAndSessionTime(Integer mentorId, LocalDate sessionDate, LocalTime sessionTime);

    List<MentoringSession> findByMentorIdAndSessionDateBetweenOrderBySessionDateDescSessionTimeDesc(
            Integer mentorId, LocalDate startDate, LocalDate endDate);
}
