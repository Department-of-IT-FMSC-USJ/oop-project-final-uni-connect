package com.uniconnect.controller;

import com.uniconnect.dto.EligibleStudentResponse;
import com.uniconnect.model.User;
import com.uniconnect.service.PointService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/mentors")
public class MentorController {

    private final PointService pointService;

    public MentorController(PointService pointService) {
        this.pointService = pointService;
    }

    @GetMapping("/eligible")
    public ResponseEntity<List<EligibleStudentResponse>> getEligible(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(pointService.getEligibleStudents(user));
    }
}
