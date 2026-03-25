package com.uniconnect.hod.modules.sessions.controller;

import com.uniconnect.model.Role;
import com.uniconnect.model.User;
import com.uniconnect.repository.UserRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/hod/sessions")
public class HodSessionController {

    private final UserRepository userRepository;
    private final com.uniconnect.academic.modules.sessions.repository.MentoringSessionRepository academicSessionRepo;
    private final com.uniconnect.industry.modules.sessions.repository.MentoringSessionRepository industrySessionRepo;

    public HodSessionController(
            UserRepository userRepository,
            @Qualifier("academicMentoringSessionRepository")
            com.uniconnect.academic.modules.sessions.repository.MentoringSessionRepository academicSessionRepo,
            @Qualifier("industryMentoringSessionRepository")
            com.uniconnect.industry.modules.sessions.repository.MentoringSessionRepository industrySessionRepo
    ) {
        this.userRepository = userRepository;
        this.academicSessionRepo = academicSessionRepo;
        this.industrySessionRepo = industrySessionRepo;
    }

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getDepartmentSessions(
            @AuthenticationPrincipal User currentUser
    ) {
        if (currentUser.getRole() != Role.DEPARTMENT_HEAD
                && currentUser.getRole() != Role.DEPARTMENT_ASSISTANT) {
            return ResponseEntity.status(403).build();
        }

        String department = currentUser.getDepartment();
        List<User> academicMentors = userRepository.findByRoleAndDepartmentAndActiveTrue(Role.ACADEMIC_MENTOR, department);
        List<User> industryMentors = userRepository.findByRoleAndDepartmentAndActiveTrue(Role.INDUSTRY_MENTOR, department);

        List<Map<String, Object>> allSessions = new ArrayList<>();

        for (User mentor : academicMentors) {
            var sessions = academicSessionRepo.findByMentorIdOrderBySessionDateDescSessionTimeDesc(mentor.getId().intValue());
            for (var session : sessions) {
                Map<String, Object> entry = new LinkedHashMap<>();
                entry.put("sessionId", session.getSessionId());
                entry.put("mentorName", mentor.getFullName());
                entry.put("mentorType", "Academic");
                entry.put("sessionTitle", session.getSessionTitle());
                entry.put("sessionDate", session.getSessionDate() != null ? session.getSessionDate().toString() : null);
                entry.put("sessionTime", session.getSessionTime() != null ? session.getSessionTime().toString() : null);
                entry.put("sessionType", session.getSessionType() != null ? session.getSessionType().name() : null);
                entry.put("audienceMode", session.getAudienceMode());
                allSessions.add(entry);
            }
        }

        for (User mentor : industryMentors) {
            var sessions = industrySessionRepo.findByMentorIdOrderBySessionDateDescSessionTimeDesc(mentor.getId().intValue());
            for (var session : sessions) {
                Map<String, Object> entry = new LinkedHashMap<>();
                entry.put("sessionId", session.getSessionId());
                entry.put("mentorName", mentor.getFullName());
                entry.put("mentorType", "Industry");
                entry.put("sessionTitle", session.getSessionTitle());
                entry.put("sessionDate", session.getSessionDate() != null ? session.getSessionDate().toString() : null);
                entry.put("sessionTime", session.getSessionTime() != null ? session.getSessionTime().toString() : null);
                entry.put("sessionType", session.getSessionType() != null ? session.getSessionType().name() : null);
                entry.put("audienceMode", session.getAudienceMode());
                allSessions.add(entry);
            }
        }

        allSessions.sort((a, b) -> {
            String dateA = String.valueOf(a.getOrDefault("sessionDate", ""));
            String dateB = String.valueOf(b.getOrDefault("sessionDate", ""));
            return dateB.compareTo(dateA);
        });

        return ResponseEntity.ok(allSessions);
    }
}
