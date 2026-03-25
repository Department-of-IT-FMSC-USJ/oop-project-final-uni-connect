package com.uniconnect.scheduling.service;

import com.uniconnect.model.Role;
import com.uniconnect.model.User;
import com.uniconnect.repository.UserRepository;
import com.uniconnect.scheduling.config.SchedulingProperties;
import com.uniconnect.scheduling.dto.SessionCreateRequest;
import com.uniconnect.scheduling.dto.SessionParticipantResponse;
import com.uniconnect.scheduling.dto.SessionResponse;
import com.uniconnect.scheduling.dto.SessionUpdateRequest;
import com.uniconnect.scheduling.events.SessionCancelledEvent;
import com.uniconnect.scheduling.events.SessionCreatedEvent;
import com.uniconnect.scheduling.events.SessionUpdatedEvent;
import com.uniconnect.scheduling.meeting.MeetingDetails;
import com.uniconnect.scheduling.meeting.MeetingProvider;
import com.uniconnect.scheduling.meeting.MeetingProviderRegistry;
import com.uniconnect.scheduling.meeting.MeetingRequest;
import com.uniconnect.scheduling.model.MeetingProviderType;
import com.uniconnect.scheduling.model.SessionEvent;
import com.uniconnect.scheduling.model.SessionParticipant;
import com.uniconnect.scheduling.model.SessionParticipantRole;
import com.uniconnect.scheduling.model.SessionStatus;
import com.uniconnect.scheduling.repository.SessionEventRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
public class SessionSchedulingServiceImpl implements SessionSchedulingService {

    private static final Set<SessionStatus> TERMINAL_STATUSES = Set.of(SessionStatus.CANCELLED, SessionStatus.COMPLETED, SessionStatus.MISSED);
    private static final Set<SessionStatus> CONFLICT_STATUSES = Set.of(SessionStatus.CREATED, SessionStatus.SCHEDULED);
    private static final DateTimeFormatter ICS_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'");

    private final SessionEventRepository sessionEventRepository;
    private final UserRepository userRepository;
    private final MeetingProviderRegistry meetingProviderRegistry;
    private final SchedulingProperties schedulingProperties;
    private final ApplicationEventPublisher eventPublisher;

    public SessionSchedulingServiceImpl(
            SessionEventRepository sessionEventRepository,
            UserRepository userRepository,
            MeetingProviderRegistry meetingProviderRegistry,
            SchedulingProperties schedulingProperties,
            ApplicationEventPublisher eventPublisher
    ) {
        this.sessionEventRepository = sessionEventRepository;
        this.userRepository = userRepository;
        this.meetingProviderRegistry = meetingProviderRegistry;
        this.schedulingProperties = schedulingProperties;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public SessionResponse createSession(User actor, SessionCreateRequest request) {
        requireCanCreate(actor);
        validateTimingRules(request.getStartsAt(), request.getDurationMinutes());

        Set<Long> participantIds = normalizeParticipantIds(request.getParticipantUserIds(), actor.getId());
        Map<Long, User> usersById = fetchUsers(participantIds);
        validateParticipantRoles(usersById.values());

        LocalDateTime requestedEnd = request.getStartsAt().plusMinutes(request.getDurationMinutes());
        validateNoConflicts(request.getStartsAt(), requestedEnd, usersById.values(), null);

        SessionEvent session = new SessionEvent();
        session.setTitle(request.getTitle().trim());
        session.setTopic(normalizeNullable(request.getTopic()));
        session.setDescription(normalizeNullable(request.getDescription()));
        session.setStartsAt(request.getStartsAt());
        session.setDurationMinutes(request.getDurationMinutes());
        session.setStatus(SessionStatus.SCHEDULED);
        session.setCreatorUserId(actor.getId());

        MeetingProviderType providerType = request.getMeetingProvider() != null
                ? request.getMeetingProvider()
                : schedulingProperties.getMeeting().getDefaultProvider();
        session.setMeetingProvider(providerType);

        session = sessionEventRepository.save(session);

        MeetingProvider provider = meetingProviderRegistry.resolve(providerType);
        MeetingDetails details = provider.createMeeting(new MeetingRequest(
                providerType,
                session.getTitle(),
                session.getStartsAt(),
                session.getDurationMinutes(),
                session.getId()
        ));
        session.setMeetingJoinUrl(details.getJoinUrl());
        session.setMeetingReference(details.getReference());

        replaceParticipants(session, usersById);
        session = sessionEventRepository.save(session);

        eventPublisher.publishEvent(new SessionCreatedEvent(
                session.getId(),
                "Session Scheduled",
                "A session '" + session.getTitle() + "' was scheduled for " + session.getStartsAt() + ".",
                new ArrayList<>(participantIds)
        ));

        return mapToResponse(session, usersById);
    }

    @Override
    public SessionResponse updateSession(User actor, Long sessionId, SessionUpdateRequest request) {
        SessionEvent session = getSessionOrThrow(sessionId);
        requireOwnerOrHodAssistant(actor, session);
        syncMissedIfNeeded(session);
        ensureMutable(session);
        validateTimingRules(request.getStartsAt(), request.getDurationMinutes());

        Set<Long> participantIds = normalizeParticipantIds(request.getParticipantUserIds(), session.getCreatorUserId());
        Map<Long, User> usersById = fetchUsers(participantIds);
        validateParticipantRoles(usersById.values());

        LocalDateTime requestedEnd = request.getStartsAt().plusMinutes(request.getDurationMinutes());
        validateNoConflicts(request.getStartsAt(), requestedEnd, usersById.values(), session.getId());

        session.setTitle(request.getTitle().trim());
        session.setTopic(normalizeNullable(request.getTopic()));
        session.setDescription(normalizeNullable(request.getDescription()));
        session.setStartsAt(request.getStartsAt());
        session.setDurationMinutes(request.getDurationMinutes());
        session.setReminderSentAt(null);

        replaceParticipants(session, usersById);
        session = sessionEventRepository.save(session);

        eventPublisher.publishEvent(new SessionUpdatedEvent(
                session.getId(),
                "Session Updated",
                "Session '" + session.getTitle() + "' was updated. Please review the new schedule.",
                new ArrayList<>(participantIds)
        ));

        return mapToResponse(session, usersById);
    }

    @Override
    public SessionResponse updateStatus(User actor, Long sessionId, SessionStatus status) {
        SessionEvent session = getSessionOrThrow(sessionId);
        requireOwnerOrHodAssistant(actor, session);
        syncMissedIfNeeded(session);
        validateStatusTransition(session.getStatus(), status);

        session.setStatus(status);
        session = sessionEventRepository.save(session);

        Map<Long, User> usersById = fetchUsers(collectParticipantIds(session));

        if (status == SessionStatus.CANCELLED) {
            eventPublisher.publishEvent(new SessionCancelledEvent(
                    session.getId(),
                    "Session Cancelled",
                    "Session '" + session.getTitle() + "' has been cancelled.",
                    new ArrayList<>(usersById.keySet())
            ));
        } else {
            eventPublisher.publishEvent(new SessionUpdatedEvent(
                    session.getId(),
                    "Session Status Updated",
                    "Session '" + session.getTitle() + "' status changed to " + status + ".",
                    new ArrayList<>(usersById.keySet())
            ));
        }

        return mapToResponse(session, usersById);
    }

    @Override
    @Transactional(readOnly = true)
    public SessionResponse getById(User actor, Long sessionId) {
        SessionEvent session = getSessionOrThrow(sessionId);
        requireCanView(actor, session);
        Map<Long, User> usersById = fetchUsers(collectParticipantIds(session));
        return mapToResponse(session, usersById);
    }

    @Override
    public List<SessionResponse> getCalendar(User actor, LocalDateTime from, LocalDateTime to, Long userId) {
        if (to.isBefore(from)) {
            throw new IllegalArgumentException("Invalid range: 'to' must be after 'from'.");
        }

        Long targetUserId = resolveCalendarTarget(actor, userId);
        List<SessionEvent> sessions = (targetUserId == null)
                ? sessionEventRepository.findCalendarInRange(from, to)
                : sessionEventRepository.findCalendarForUser(targetUserId, from, to);

        normalizeMissedStatuses(sessions);
        Map<Long, User> usersById = fetchUsers(collectAllParticipantIds(sessions));

        return sessions.stream()
                .map(session -> mapToResponse(session, usersById))
                .sorted(Comparator.comparing(SessionResponse::getStart))
                .toList();
    }

    @Override
    public List<SessionResponse> getUpcoming(User actor) {
        List<SessionEvent> sessions = sessionEventRepository.findUpcomingForUser(actor.getId(), LocalDateTime.now());
        normalizeMissedStatuses(sessions);
        Map<Long, User> usersById = fetchUsers(collectAllParticipantIds(sessions));
        return sessions.stream().map(session -> mapToResponse(session, usersById)).toList();
    }

    @Override
    public List<SessionResponse> getMySessions(User actor) {
        List<SessionEvent> sessions = sessionEventRepository.findAllForUser(actor.getId());
        normalizeMissedStatuses(sessions);
        Map<Long, User> usersById = fetchUsers(collectAllParticipantIds(sessions));
        return sessions.stream().map(session -> mapToResponse(session, usersById)).toList();
    }

    @Override
    public List<SessionResponse> getMentorSessions(User actor, Long mentorUserId) {
        User mentor = userRepository.findById(mentorUserId)
                .orElseThrow(() -> new IllegalArgumentException("Mentor not found: " + mentorUserId));
        if (mentor.getRole() != Role.ACADEMIC_MENTOR && mentor.getRole() != Role.INDUSTRY_MENTOR) {
            throw new IllegalArgumentException("Provided user is not a mentor.");
        }
        if (!isHodWorkspace(actor) && !Objects.equals(actor.getId(), mentorUserId)) {
            throw new IllegalArgumentException("You are not allowed to view another mentor's sessions.");
        }

        List<SessionEvent> sessions = sessionEventRepository.findByMentorAndStudent(mentorUserId, null);
        normalizeMissedStatuses(sessions);
        Map<Long, User> usersById = fetchUsers(collectAllParticipantIds(sessions));
        return sessions.stream().map(session -> mapToResponse(session, usersById)).toList();
    }

    @Override
    public List<SessionResponse> getStudentSessions(User actor, Long studentUserId) {
        User student = userRepository.findById(studentUserId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found: " + studentUserId));
        if (student.getRole() != Role.UNDERGRADUATE) {
            throw new IllegalArgumentException("Provided user is not a student.");
        }
        if (!isHodWorkspace(actor) && !Objects.equals(actor.getId(), studentUserId)) {
            throw new IllegalArgumentException("You are not allowed to view another student's sessions.");
        }

        List<SessionEvent> sessions = sessionEventRepository.findByMentorAndStudent(null, studentUserId);
        normalizeMissedStatuses(sessions);
        Map<Long, User> usersById = fetchUsers(collectAllParticipantIds(sessions));
        return sessions.stream().map(session -> mapToResponse(session, usersById)).toList();
    }

    @Override
    public List<SessionResponse> listSessions(User actor) {
        if (!isHodWorkspace(actor)) {
            return getMySessions(actor);
        }
        List<SessionEvent> sessions = sessionEventRepository.findAll();
        normalizeMissedStatuses(sessions);
        Map<Long, User> usersById = fetchUsers(collectAllParticipantIds(sessions));
        return sessions.stream()
                .sorted(Comparator.comparing(SessionEvent::getStartsAt).reversed())
                .map(session -> mapToResponse(session, usersById))
                .toList();
    }

    @Override
    public SessionResponse cancelSession(User actor, Long sessionId) {
        return updateStatus(actor, sessionId, SessionStatus.CANCELLED);
    }

    @Override
    public SessionResponse rescheduleSession(User actor, Long sessionId, SessionUpdateRequest request) {
        return updateSession(actor, sessionId, request);
    }

    @Override
    public String resolveJoinUrl(User actor, Long sessionId) {
        SessionEvent session = getSessionOrThrow(sessionId);
        requireCanView(actor, session);
        syncMissedIfNeeded(session);

        if (session.getStatus() == SessionStatus.CANCELLED) {
            throw new IllegalStateException("Cancelled sessions cannot be joined.");
        }
        if (session.getStatus() == SessionStatus.COMPLETED || session.getStatus() == SessionStatus.MISSED) {
            throw new IllegalStateException("This session is closed and cannot be joined.");
        }
        if (session.getMeetingJoinUrl() == null || session.getMeetingJoinUrl().isBlank()) {
            throw new IllegalStateException("No meeting URL is available for this session.");
        }
        return session.getMeetingJoinUrl();
    }

    @Override
    public String buildIcs(User actor, Long sessionId) {
        SessionEvent session = getSessionOrThrow(sessionId);
        requireCanView(actor, session);

        String summary = escapeIcs(session.getTitle());
        String description = escapeIcs(session.getDescription() == null ? "Mentoring session" : session.getDescription());
        String location = escapeIcs(session.getMeetingJoinUrl() == null ? "Online" : session.getMeetingJoinUrl());

        String dtStamp = LocalDateTime.now().atOffset(ZoneOffset.UTC).format(ICS_FORMATTER);
        String dtStart = session.getStartsAt().atOffset(ZoneOffset.UTC).format(ICS_FORMATTER);
        String dtEnd = session.getEndsAt().atOffset(ZoneOffset.UTC).format(ICS_FORMATTER);

        return "BEGIN:VCALENDAR\n"
                + "VERSION:2.0\n"
                + "PRODID:-//UniConnect//Scheduling//EN\n"
                + "CALSCALE:GREGORIAN\n"
                + "METHOD:PUBLISH\n"
                + "BEGIN:VEVENT\n"
                + "UID:session-" + session.getId() + "@uniconnect\n"
                + "DTSTAMP:" + dtStamp + "\n"
                + "DTSTART:" + dtStart + "\n"
                + "DTEND:" + dtEnd + "\n"
                + "SUMMARY:" + summary + "\n"
                + "DESCRIPTION:" + description + "\n"
                + "LOCATION:" + location + "\n"
                + "STATUS:" + mapIcsStatus(session.getStatus()) + "\n"
                + "END:VEVENT\n"
                + "END:VCALENDAR\n";
    }

    private void validateTimingRules(LocalDateTime startsAt, Integer durationMinutes) {
        if (startsAt == null) {
            throw new IllegalArgumentException("Session start time is required.");
        }
        if (!startsAt.isAfter(LocalDateTime.now())) {
            throw new IllegalArgumentException("Session start time must be in the future.");
        }
        int minimum = Math.max(1, schedulingProperties.getMeeting().getMinimumDurationMinutes());
        if (durationMinutes == null || durationMinutes < minimum) {
            throw new IllegalArgumentException("Session duration must be at least " + minimum + " minutes.");
        }
    }

    private void validateNoConflicts(
            LocalDateTime startsAt,
            LocalDateTime endsAt,
            Iterable<User> participants,
            Long excludeSessionId
    ) {
        Set<Long> mentorIds = new LinkedHashSet<>();
        Set<Long> studentIds = new LinkedHashSet<>();

        for (User user : participants) {
            if (user.getRole() == Role.ACADEMIC_MENTOR || user.getRole() == Role.INDUSTRY_MENTOR) {
                mentorIds.add(user.getId());
            } else if (user.getRole() == Role.UNDERGRADUATE) {
                studentIds.add(user.getId());
            }
        }

        LocalDateTime windowStart = startsAt.minusHours(12);
        LocalDateTime windowEnd = endsAt.plusHours(12);

        for (Long mentorId : mentorIds) {
            List<SessionEvent> mentorSessions = sessionEventRepository.findByCreatorUserIdAndStartsAtBetween(mentorId, windowStart, windowEnd);
            for (SessionEvent existing : mentorSessions) {
                if (Objects.equals(existing.getId(), excludeSessionId) || !CONFLICT_STATUSES.contains(existing.getStatus())) {
                    continue;
                }
                if (overlaps(startsAt, endsAt, existing.getStartsAt(), existing.getEndsAt())) {
                    throw new IllegalArgumentException("Mentor has an overlapping session at " + existing.getStartsAt() + ".");
                }
            }
        }

        if (!studentIds.isEmpty()) {
            List<SessionEvent> studentSessions = sessionEventRepository.findByParticipantIdsInRange(studentIds, windowStart, windowEnd, excludeSessionId);
            for (SessionEvent existing : studentSessions) {
                if (!CONFLICT_STATUSES.contains(existing.getStatus())) {
                    continue;
                }
                if (overlaps(startsAt, endsAt, existing.getStartsAt(), existing.getEndsAt())) {
                    throw new IllegalArgumentException("One or more students already have another session at " + existing.getStartsAt() + ".");
                }
            }
        }
    }

    private boolean overlaps(LocalDateTime startA, LocalDateTime endA, LocalDateTime startB, LocalDateTime endB) {
        if (startA == null || endA == null || startB == null || endB == null) {
            return false;
        }
        return startA.isBefore(endB) && startB.isBefore(endA);
    }

    private SessionEvent getSessionOrThrow(Long sessionId) {
        return sessionEventRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Session not found: " + sessionId));
    }

    private void requireCanCreate(User actor) {
        if (actor == null || actor.getRole() == null) {
            throw new IllegalArgumentException("Authenticated user is required.");
        }
        if (actor.getRole() != Role.ACADEMIC_MENTOR
                && actor.getRole() != Role.INDUSTRY_MENTOR
                && actor.getRole() != Role.DEPARTMENT_HEAD) {
            throw new IllegalArgumentException("Only mentors or department heads can schedule sessions.");
        }
    }

    private void requireOwnerOrHodAssistant(User actor, SessionEvent session) {
        if (actor == null || actor.getRole() == null) {
            throw new IllegalArgumentException("Authenticated user is required.");
        }
        boolean owner = actor.getId() != null && actor.getId().equals(session.getCreatorUserId());
        if (!owner && !isHodWorkspace(actor)) {
            throw new IllegalArgumentException("You are not allowed to modify this session.");
        }
    }

    private void requireCanView(User actor, SessionEvent session) {
        if (actor == null || actor.getId() == null) {
            throw new IllegalArgumentException("Authenticated user is required.");
        }
        boolean creator = actor.getId().equals(session.getCreatorUserId());
        boolean participant = collectParticipantIds(session).contains(actor.getId());
        if (!creator && !participant && !isHodWorkspace(actor)) {
            throw new IllegalArgumentException("You are not allowed to view this session.");
        }
    }

    private Long resolveCalendarTarget(User actor, Long requestedUserId) {
        if (requestedUserId == null) {
            return actor.getId();
        }

        if (requestedUserId.equals(actor.getId())) {
            return requestedUserId;
        }

        if (isHodWorkspace(actor)) {
            return requestedUserId;
        }

        throw new IllegalArgumentException("You can only access your own calendar.");
    }

    private boolean isHodWorkspace(User actor) {
        return actor.getRole() == Role.DEPARTMENT_HEAD || actor.getRole() == Role.DEPARTMENT_ASSISTANT;
    }

    private void replaceParticipants(SessionEvent session, Map<Long, User> usersById) {
        session.clearParticipants();
        for (User user : usersById.values()) {
            SessionParticipant participant = new SessionParticipant();
            participant.setUserId(user.getId());
            participant.setParticipantRole(resolveParticipantRole(user.getRole()));
            session.addParticipant(participant);
        }
    }

    private Set<Long> normalizeParticipantIds(List<Long> participantUserIds, Long creatorUserId) {
        Set<Long> ids = participantUserIds == null
                ? new LinkedHashSet<>()
                : participantUserIds.stream()
                .filter(id -> id != null && id > 0)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        if (creatorUserId != null) {
            ids.add(creatorUserId);
        }
        if (ids.isEmpty()) {
            throw new IllegalArgumentException("At least one participant is required.");
        }
        return ids;
    }

    private Map<Long, User> fetchUsers(Set<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Map.of();
        }
        List<User> users = userRepository.findAllById(userIds);
        Map<Long, User> usersById = users.stream().collect(Collectors.toMap(User::getId, user -> user));
        if (usersById.size() != userIds.size()) {
            throw new IllegalArgumentException("One or more participants were not found.");
        }
        return usersById;
    }

    private void validateParticipantRoles(Iterable<User> users) {
        for (User user : users) {
            SessionParticipantRole role = resolveParticipantRole(user.getRole());
            if (role != SessionParticipantRole.STUDENT
                    && role != SessionParticipantRole.MENTOR
                    && role != SessionParticipantRole.HOD) {
                throw new IllegalArgumentException("Participants can only be students, mentors, or HOD users.");
            }
        }
    }

    private SessionParticipantRole resolveParticipantRole(Role role) {
        if (role == null) {
            throw new IllegalArgumentException("User role is missing for participant.");
        }
        return switch (role) {
            case UNDERGRADUATE -> SessionParticipantRole.STUDENT;
            case ACADEMIC_MENTOR, INDUSTRY_MENTOR -> SessionParticipantRole.MENTOR;
            case DEPARTMENT_HEAD -> SessionParticipantRole.HOD;
            case DEPARTMENT_ASSISTANT -> SessionParticipantRole.ASSISTANT;
        };
    }

    private void ensureMutable(SessionEvent session) {
        if (TERMINAL_STATUSES.contains(session.getStatus())) {
            throw new IllegalStateException("Terminal sessions cannot be modified.");
        }
    }

    private void validateStatusTransition(SessionStatus currentStatus, SessionStatus nextStatus) {
        if (currentStatus == nextStatus) {
            return;
        }
        if (TERMINAL_STATUSES.contains(currentStatus)) {
            throw new IllegalStateException("Session is already terminal with status: " + currentStatus);
        }
        if (nextStatus == SessionStatus.CREATED) {
            throw new IllegalArgumentException("Cannot transition back to CREATED.");
        }
    }

    private Set<Long> collectParticipantIds(SessionEvent session) {
        Set<Long> ids = new LinkedHashSet<>();
        ids.add(session.getCreatorUserId());
        for (SessionParticipant participant : session.getParticipants()) {
            ids.add(participant.getUserId());
        }
        return ids;
    }

    private Set<Long> collectAllParticipantIds(List<SessionEvent> sessions) {
        Set<Long> ids = new LinkedHashSet<>();
        for (SessionEvent session : sessions) {
            ids.addAll(collectParticipantIds(session));
        }
        return ids.stream().filter(Objects::nonNull).collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private void normalizeMissedStatuses(List<SessionEvent> sessions) {
        for (SessionEvent session : sessions) {
            syncMissedIfNeeded(session);
        }
    }

    private void syncMissedIfNeeded(SessionEvent session) {
        if (session.getStatus() != SessionStatus.CREATED && session.getStatus() != SessionStatus.SCHEDULED) {
            return;
        }
        LocalDateTime endsAt = session.getEndsAt();
        if (endsAt != null && endsAt.isBefore(LocalDateTime.now())) {
            session.setStatus(SessionStatus.MISSED);
            sessionEventRepository.save(session);
        }
    }

    private SessionResponse mapToResponse(SessionEvent session, Map<Long, User> usersById) {
        LocalDateTime endsAt = session.getEndsAt();
        SessionResponse response = new SessionResponse();
        response.setId(session.getId());
        response.setTitle(session.getTitle());
        response.setTopic(session.getTopic());
        response.setDescription(session.getDescription());
        response.setStart(session.getStartsAt());
        response.setStartsAt(session.getStartsAt());
        response.setEnd(endsAt);
        response.setEndsAt(endsAt);
        response.setDurationMinutes(session.getDurationMinutes());
        response.setStatus(session.getStatus());
        response.setCreatorUserId(session.getCreatorUserId());
        response.setMeetingProvider(session.getMeetingProvider());
        response.setJoinUrl(session.getMeetingJoinUrl());
        response.setMeetingJoinUrl(session.getMeetingJoinUrl());
        response.setMeetingReference(session.getMeetingReference());
        response.setCanJoin(session.getStatus() == SessionStatus.SCHEDULED
                && session.getMeetingJoinUrl() != null
                && (endsAt == null || endsAt.isAfter(LocalDateTime.now())));
        response.setCreatedAt(session.getCreatedAt());
        response.setUpdatedAt(session.getUpdatedAt());

        List<SessionParticipantResponse> participants = session.getParticipants().stream()
                .map(participant -> {
                    SessionParticipantResponse item = new SessionParticipantResponse();
                    item.setUserId(participant.getUserId());
                    User user = usersById.get(participant.getUserId());
                    item.setFullName(user == null ? null : user.getFullName());
                    item.setEmail(user == null ? null : user.getEmail());
                    item.setParticipantRole(participant.getParticipantRole());
                    return item;
                })
                .sorted(Comparator.comparing(SessionParticipantResponse::getUserId))
                .toList();
        response.setParticipants(participants);

        return response;
    }

    private String normalizeNullable(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String mapIcsStatus(SessionStatus status) {
        return switch (status) {
            case CANCELLED -> "CANCELLED";
            case COMPLETED -> "CONFIRMED";
            case MISSED -> "CONFIRMED";
            case CREATED, SCHEDULED -> "CONFIRMED";
        };
    }

    private String escapeIcs(String value) {
        return value
                .replace("\\", "\\\\")
                .replace(";", "\\;")
                .replace(",", "\\,")
                .replace("\n", "\\n");
    }
}
