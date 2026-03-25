package com.uniconnect.controller;

import com.uniconnect.dto.StudentMentorSessionResponse;
import com.uniconnect.model.Role;
import com.uniconnect.model.User;
import com.uniconnect.repository.UserRepository;
import com.uniconnect.student.modules.mentor.entity.MentorConnection;
import com.uniconnect.student.modules.mentor.enums.ConnectionStatus;
import com.uniconnect.student.modules.mentor.repository.MentorConnectionRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/student-sessions")
public class StudentSessionController {

    private final MentorConnectionRepository mentorConnectionRepository;
    private final UserRepository userRepository;
    private final com.uniconnect.academic.modules.sessions.repository.MentoringSessionRepository academicSessionRepository;
    private final com.uniconnect.industry.modules.sessions.repository.MentoringSessionRepository industrySessionRepository;

    public StudentSessionController(
            MentorConnectionRepository mentorConnectionRepository,
            UserRepository userRepository,
            com.uniconnect.academic.modules.sessions.repository.MentoringSessionRepository academicSessionRepository,
            com.uniconnect.industry.modules.sessions.repository.MentoringSessionRepository industrySessionRepository
    ) {
        this.mentorConnectionRepository = mentorConnectionRepository;
        this.userRepository = userRepository;
        this.academicSessionRepository = academicSessionRepository;
        this.industrySessionRepository = industrySessionRepository;
    }

    @GetMapping("/{studentId}")
    public ResponseEntity<List<StudentMentorSessionResponse>> getStudentSessions(
            @AuthenticationPrincipal User currentUser,
            @PathVariable Integer studentId
    ) {
        if (currentUser.getRole() != Role.UNDERGRADUATE || !currentUser.getId().equals(studentId.longValue())) {
            return ResponseEntity.status(403).build();
        }

        List<MentorConnection> connections = mentorConnectionRepository.findByStudentIdOrderByCreatedDateDesc(studentId)
                .stream()
                .filter(connection -> connection.getConnectionStatus() == ConnectionStatus.Approved)
                .toList();

        List<StudentMentorSessionResponse> responses = new ArrayList<>();

        for (MentorConnection connection : connections) {
            User mentor = userRepository.findById(connection.getMentorId().longValue()).orElse(null);
            if (mentor == null) {
                continue;
            }

            if (mentor.getRole() == Role.ACADEMIC_MENTOR) {
                academicSessionRepository.findByMentorIdOrderBySessionDateDescSessionTimeDesc(connection.getMentorId())
                        .stream()
                        .filter(session -> isStudentTargeted(studentId, session.getTargetStudentIds()))
                        .forEach(session -> responses.add(new StudentMentorSessionResponse(
                                session.getSessionId(),
                                session.getMentorId(),
                                mentor.getFullName(),
                                mentor.getRole().name(),
                                session.getSessionTitle(),
                                session.getSessionType().name(),
                                session.getSessionTopic(),
                                session.getSessionDescription(),
                                session.getAudienceMode(),
                                session.getSessionDate(),
                                session.getSessionTime()
                        )));
            }

            if (mentor.getRole() == Role.INDUSTRY_MENTOR) {
                industrySessionRepository.findByMentorIdOrderBySessionDateDescSessionTimeDesc(connection.getMentorId())
                        .stream()
                        .filter(session -> isStudentTargeted(studentId, session.getTargetStudentIds()))
                        .forEach(session -> responses.add(new StudentMentorSessionResponse(
                                session.getSessionId(),
                                session.getMentorId(),
                                mentor.getFullName(),
                                mentor.getRole().name(),
                                session.getSessionTitle(),
                                session.getSessionType().name(),
                                session.getSessionTopic(),
                                session.getSessionDescription(),
                                session.getAudienceMode(),
                                session.getSessionDate(),
                                session.getSessionTime()
                        )));
            }
        }

        responses.sort(Comparator
                .comparing(StudentMentorSessionResponse::getSessionDate, Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(StudentMentorSessionResponse::getSessionTime, Comparator.nullsLast(Comparator.naturalOrder())));

        return ResponseEntity.ok(responses);
    }

    private boolean isStudentTargeted(Integer studentId, String targetStudentIds) {
        if (targetStudentIds == null || targetStudentIds.isBlank()) {
            return true;
        }

        Set<Integer> parsedIds = java.util.Arrays.stream(targetStudentIds.split(","))
                .map(String::trim)
                .filter(value -> !value.isEmpty())
                .map(Integer::valueOf)
                .collect(Collectors.toSet());
        return parsedIds.contains(studentId);
    }
}
