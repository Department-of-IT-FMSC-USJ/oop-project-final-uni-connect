package com.uniconnect.scheduling.service;

import com.uniconnect.model.User;
import com.uniconnect.scheduling.dto.SessionCreateRequest;
import com.uniconnect.scheduling.dto.SessionResponse;
import com.uniconnect.scheduling.dto.SessionUpdateRequest;
import com.uniconnect.scheduling.model.SessionStatus;

import java.time.LocalDateTime;
import java.util.List;

public interface SessionSchedulingService {
    SessionResponse createSession(User actor, SessionCreateRequest request);

    SessionResponse updateSession(User actor, Long sessionId, SessionUpdateRequest request);

    SessionResponse updateStatus(User actor, Long sessionId, SessionStatus status);

    SessionResponse getById(User actor, Long sessionId);

    List<SessionResponse> getCalendar(User actor, LocalDateTime from, LocalDateTime to, Long userId);

    List<SessionResponse> getUpcoming(User actor);

    List<SessionResponse> getMySessions(User actor);

    List<SessionResponse> getMentorSessions(User actor, Long mentorUserId);

    List<SessionResponse> getStudentSessions(User actor, Long studentUserId);

    List<SessionResponse> listSessions(User actor);

    SessionResponse cancelSession(User actor, Long sessionId);

    SessionResponse rescheduleSession(User actor, Long sessionId, SessionUpdateRequest request);

    String resolveJoinUrl(User actor, Long sessionId);

    String buildIcs(User actor, Long sessionId);
}

