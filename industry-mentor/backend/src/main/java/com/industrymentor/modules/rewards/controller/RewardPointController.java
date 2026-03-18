package com.industrymentor.modules.rewards.controller;

import com.industrymentor.common.dto.ApiResponseDTO;
import com.industrymentor.common.exception.UnauthorizedAccessException;
import com.industrymentor.modules.rewards.dto.RewardSummaryDTO;
import com.industrymentor.modules.rewards.service.RewardPointService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/rewards")
public class RewardPointController {

    private final RewardPointService rewardPointService;

    public RewardPointController(RewardPointService rewardPointService) {
        this.rewardPointService = rewardPointService;
    }

    @GetMapping("/mentor/{mentorId}")
    public ResponseEntity<ApiResponseDTO<RewardSummaryDTO>> getRewardSummary(
            @PathVariable("mentorId") Integer mentorId,
            @RequestHeader(value = "X-User-Role", defaultValue = "") String userRole) {

        if (!"MENTOR".equalsIgnoreCase(userRole)) {
            throw new UnauthorizedAccessException("Only logged-in industry mentors can view reward points.");
        }

        RewardSummaryDTO summary = rewardPointService.getRewardSummary(mentorId);
        ApiResponseDTO<RewardSummaryDTO> response = ApiResponseDTO.success(
                "Reward summary retrieved successfully", summary);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
