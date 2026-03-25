package com.uniconnect.scheduling.repository;

import com.uniconnect.scheduling.model.SessionEvent;
import com.uniconnect.scheduling.model.SessionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

public interface SessionEventRepository extends JpaRepository<SessionEvent, Long> {

    @Query("""
            select distinct s
            from SessionEvent s
            left join s.participants p
            where (s.creatorUserId = :userId or p.userId = :userId)
              and s.startsAt between :from and :to
            order by s.startsAt asc
            """)
    List<SessionEvent> findCalendarForUser(
            @Param("userId") Long userId,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to
    );

    @Query("""
            select distinct s
            from SessionEvent s
            where s.startsAt between :from and :to
            order by s.startsAt asc
            """)
    List<SessionEvent> findCalendarInRange(
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to
    );

    List<SessionEvent> findByCreatorUserIdAndStartsAtBetween(Long creatorUserId, LocalDateTime from, LocalDateTime to);

    @Query("""
            select distinct s
            from SessionEvent s
            join s.participants p
            where p.userId in :userIds
              and s.startsAt between :from and :to
              and (:excludeSessionId is null or s.id <> :excludeSessionId)
            """)
    List<SessionEvent> findByParticipantIdsInRange(
            @Param("userIds") Set<Long> userIds,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to,
            @Param("excludeSessionId") Long excludeSessionId
    );

    @Query("""
            select distinct s
            from SessionEvent s
            left join s.participants p
            where (s.creatorUserId = :userId or p.userId = :userId)
              and s.startsAt >= :from
            order by s.startsAt asc
            """)
    List<SessionEvent> findUpcomingForUser(@Param("userId") Long userId, @Param("from") LocalDateTime from);

    @Query("""
            select distinct s
            from SessionEvent s
            left join s.participants p
            where (s.creatorUserId = :userId or p.userId = :userId)
            order by s.startsAt desc
            """)
    List<SessionEvent> findAllForUser(@Param("userId") Long userId);

    @Query("""
            select distinct s
            from SessionEvent s
            left join s.participants p
            where (:mentorId is null or s.creatorUserId = :mentorId)
              and (:studentId is null or p.userId = :studentId)
            order by s.startsAt desc
            """)
    List<SessionEvent> findByMentorAndStudent(
            @Param("mentorId") Long mentorId,
            @Param("studentId") Long studentId
    );

    List<SessionEvent> findByStatusInAndStartsAtBefore(Set<SessionStatus> statuses, LocalDateTime threshold);

    @Query("""
            select s
            from SessionEvent s
            where s.status = :status
              and s.startsAt between :from and :to
              and s.reminderSentAt is null
            order by s.startsAt asc
            """)
    List<SessionEvent> findPendingReminders(
            @Param("status") SessionStatus status,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to
    );
}

