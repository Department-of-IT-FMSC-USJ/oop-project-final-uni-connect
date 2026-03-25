package com.uniconnect.scheduling.controller;

import com.uniconnect.model.User;
import com.uniconnect.scheduling.dto.SessionCreateRequest;
import com.uniconnect.scheduling.dto.SessionResponse;
import com.uniconnect.scheduling.dto.SessionStatusUpdateRequest;
import com.uniconnect.scheduling.dto.SessionUpdateRequest;
import com.uniconnect.scheduling.service.SessionSchedulingService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/scheduling")
public class SessionSchedulingController {

    private final SessionSchedulingService sessionSchedulingService;

    public SessionSchedulingController(SessionSchedulingService sessionSchedulingService) {
        this.sessionSchedulingService = sessionSchedulingService;
    }

    @PostMapping("/sessions")
    public ResponseEntity<?> createSession(
            @AuthenticationPrincipal User actor,
            @Valid @RequestBody SessionCreateRequest request
    ) {
        SessionResponse response = sessionSchedulingService.createSession(actor, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("success", true, "data", response));
    }

    @PutMapping("/sessions/{sessionId}")
    public ResponseEntity<?> updateSession(
            @AuthenticationPrincipal User actor,
            @PathVariable Long sessionId,
            @Valid @RequestBody SessionUpdateRequest request
    ) {
        SessionResponse response = sessionSchedulingService.updateSession(actor, sessionId, request);
        return ResponseEntity.ok(Map.of("success", true, "data", response));
    }

    @PutMapping("/sessions/{sessionId}/reschedule")
    public ResponseEntity<?> rescheduleSession(
            @AuthenticationPrincipal User actor,
            @PathVariable Long sessionId,
            @Valid @RequestBody SessionUpdateRequest request
    ) {
        SessionResponse response = sessionSchedulingService.rescheduleSession(actor, sessionId, request);
        return ResponseEntity.ok(Map.of("success", true, "data", response));
    }

    @PatchMapping("/sessions/{sessionId}/status")
    public ResponseEntity<?> updateStatus(
            @AuthenticationPrincipal User actor,
            @PathVariable Long sessionId,
            @Valid @RequestBody SessionStatusUpdateRequest request
    ) {
        SessionResponse response = sessionSchedulingService.updateStatus(actor, sessionId, request.getStatus());
        return ResponseEntity.ok(Map.of("success", true, "data", response));
    }

    @PostMapping("/sessions/{sessionId}/cancel")
    public ResponseEntity<?> cancelSession(
            @AuthenticationPrincipal User actor,
            @PathVariable Long sessionId
    ) {
        SessionResponse response = sessionSchedulingService.cancelSession(actor, sessionId);
        return ResponseEntity.ok(Map.of("success", true, "data", response));
    }

    @GetMapping("/sessions/{sessionId}")
    public ResponseEntity<?> getSession(
            @AuthenticationPrincipal User actor,
            @PathVariable Long sessionId
    ) {
        SessionResponse response = sessionSchedulingService.getById(actor, sessionId);
        return ResponseEntity.ok(Map.of("success", true, "data", response));
    }

    @GetMapping("/sessions/details/{sessionId}")
    public ResponseEntity<?> getSessionDetails(
            @AuthenticationPrincipal User actor,
            @PathVariable Long sessionId
    ) {
        SessionResponse response = sessionSchedulingService.getById(actor, sessionId);
        return ResponseEntity.ok(Map.of("success", true, "data", response));
    }

    @GetMapping("/sessions/{sessionId}/join")
    public ResponseEntity<?> resolveJoinUrl(
            @AuthenticationPrincipal User actor,
            @PathVariable Long sessionId
    ) {
        String joinUrl = sessionSchedulingService.resolveJoinUrl(actor, sessionId);
        return ResponseEntity.ok(Map.of("success", true, "data", Map.of("sessionId", sessionId, "joinUrl", joinUrl)));
    }

    @GetMapping("/sessions/{sessionId}/ics")
    public ResponseEntity<String> exportIcs(
            @AuthenticationPrincipal User actor,
            @PathVariable Long sessionId
    ) {
        String content = sessionSchedulingService.buildIcs(actor, sessionId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=session-" + sessionId + ".ics")
                .contentType(MediaType.parseMediaType("text/calendar"))
                .body(content);
    }

    @GetMapping("/calendar")
    public ResponseEntity<?> getCalendar(
            @AuthenticationPrincipal User actor,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @RequestParam(required = false) Long userId
    ) {
        List<SessionResponse> response = sessionSchedulingService.getCalendar(actor, from, to, userId);
        return ResponseEntity.ok(Map.of("success", true, "data", response));
    }

    @GetMapping("/sessions/upcoming")
    public ResponseEntity<?> getUpcomingSessions(@AuthenticationPrincipal User actor) {
        List<SessionResponse> response = sessionSchedulingService.getUpcoming(actor);
        return ResponseEntity.ok(Map.of("success", true, "data", response));
    }

    @GetMapping("/sessions/my")
    public ResponseEntity<?> getMySessions(@AuthenticationPrincipal User actor) {
        List<SessionResponse> response = sessionSchedulingService.getMySessions(actor);
        return ResponseEntity.ok(Map.of("success", true, "data", response));
    }

    @GetMapping("/sessions/mentor/{mentorUserId}")
    public ResponseEntity<?> getMentorSessions(
            @AuthenticationPrincipal User actor,
            @PathVariable Long mentorUserId
    ) {
        List<SessionResponse> response = sessionSchedulingService.getMentorSessions(actor, mentorUserId);
        return ResponseEntity.ok(Map.of("success", true, "data", response));
    }

    @GetMapping("/sessions/student/{studentUserId}")
    public ResponseEntity<?> getStudentSessions(
            @AuthenticationPrincipal User actor,
            @PathVariable Long studentUserId
    ) {
        List<SessionResponse> response = sessionSchedulingService.getStudentSessions(actor, studentUserId);
        return ResponseEntity.ok(Map.of("success", true, "data", response));
    }

    @GetMapping("/sessions/list")
    public ResponseEntity<?> listSessions(@AuthenticationPrincipal User actor) {
        List<SessionResponse> response = sessionSchedulingService.listSessions(actor);
        return ResponseEntity.ok(Map.of("success", true, "data", response));
    }
}
