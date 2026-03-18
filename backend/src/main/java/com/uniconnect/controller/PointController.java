package com.uniconnect.controller;

import com.uniconnect.dto.AllocatePointsRequest;
import com.uniconnect.dto.PendingAdjustRequest;
import com.uniconnect.dto.PointAuditResponse;
import com.uniconnect.dto.PointRecordResponse;
import com.uniconnect.dto.ReviewPointsRequest;
import com.uniconnect.model.User;
import com.uniconnect.service.PointService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/points")
public class PointController {

    private final PointService pointService;

    public PointController(PointService pointService) {
        this.pointService = pointService;
    }

    @PostMapping("/allocate")
    public ResponseEntity<PointRecordResponse> allocatePoints(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody AllocatePointsRequest request) {
        return ResponseEntity.ok(pointService.allocatePoints(user, request));
    }

    @GetMapping("/pending")
    public ResponseEntity<List<PointRecordResponse>> getPending(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(pointService.getPendingPoints(user));
    }

    @PutMapping("/{recordId}/review")
    public ResponseEntity<PointRecordResponse> review(
            @AuthenticationPrincipal User user,
            @PathVariable Long recordId,
            @Valid @RequestBody ReviewPointsRequest request) {
        return ResponseEntity.ok(pointService.reviewPoints(user, recordId, request));
    }

    @PutMapping("/{recordId}/pending-adjust")
    public ResponseEntity<PointRecordResponse> adjustPending(
            @AuthenticationPrincipal User user,
            @PathVariable Long recordId,
            @Valid @RequestBody PendingAdjustRequest request) {
        return ResponseEntity.ok(pointService.adjustPendingPoints(user, recordId, request));
    }

    @PutMapping("/{recordId}/undo")
    public ResponseEntity<PointRecordResponse> undoReview(
            @AuthenticationPrincipal User user,
            @PathVariable Long recordId) {
        return ResponseEntity.ok(pointService.undoReview(user, recordId));
    }

    @GetMapping("/my")
    public ResponseEntity<List<PointRecordResponse>> getMyPoints(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(pointService.getPointsForStudent(user));
    }

    @GetMapping("/{recordId}/audit")
    public ResponseEntity<List<PointAuditResponse>> getAudit(
            @AuthenticationPrincipal User user,
            @PathVariable Long recordId) {
        return ResponseEntity.ok(pointService.getAuditForRecord(user, recordId));
    }
}
