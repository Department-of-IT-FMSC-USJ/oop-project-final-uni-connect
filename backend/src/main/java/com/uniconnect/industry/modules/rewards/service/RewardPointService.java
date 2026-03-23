package com.uniconnect.industry.modules.rewards.service;

import com.uniconnect.industry.modules.rewards.dto.RewardSummaryDTO;

public interface RewardPointService {

    RewardSummaryDTO getRewardSummary(Integer mentorId);
}
