package com.uniconnect.controller;

import com.uniconnect.dto.ProofSubmissionRequest;
import com.uniconnect.dto.ProofSubmissionResponse;
import com.uniconnect.model.Role;
import com.uniconnect.model.User;
import com.uniconnect.service.ProofService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/proofs")
public class ProofController {

    private final ProofService proofService;

    public ProofController(ProofService proofService) {
        this.proofService = proofService;
    }

    @PostMapping
    public ResponseEntity<ProofSubmissionResponse> submitProof(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody ProofSubmissionRequest request) {
        ProofSubmissionResponse response = proofService.submitProof(user, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/my")
    public ResponseEntity<List<ProofSubmissionResponse>> getMyProofs(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(proofService.getProofsForStudent(user));
    }

    @GetMapping
    public ResponseEntity<List<ProofSubmissionResponse>> getProofsForReps(
            @AuthenticationPrincipal User user,
            @RequestParam Optional<Long> studentId) {
        if (user.getRole() != Role.DEPARTMENT_HEAD && user.getRole() != Role.DEPARTMENT_ASSISTANT) {
            throw new IllegalArgumentException("Only department heads or assistants can view proofs.");
        }
        return ResponseEntity.ok(proofService.getProofsForReps(studentId));
    }
}
