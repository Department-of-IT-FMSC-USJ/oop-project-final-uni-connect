package com.industrymentor.modules.rewards.service;

import com.industrymentor.modules.rewards.dto.RewardSummaryDTO;

public interface RewardPointService {

    RewardSummaryDTO getRewardSummary(Integer mentorId);
}
