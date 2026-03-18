package com.industrymentor.modules.rewards.serviceImpl;

import com.industrymentor.modules.rewards.dto.RewardPointResponseDTO;
import com.industrymentor.modules.rewards.dto.RewardSummaryDTO;
import com.industrymentor.modules.rewards.entity.RewardPointEntry;
import com.industrymentor.modules.rewards.enums.ContributionType;
import com.industrymentor.modules.rewards.repository.RewardPointEntryRepository;
import com.industrymentor.modules.rewards.service.RewardPointService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class RewardPointServiceImpl implements RewardPointService {

    private final RewardPointEntryRepository rewardPointEntryRepository;

    public RewardPointServiceImpl(RewardPointEntryRepository rewardPointEntryRepository) {
        this.rewardPointEntryRepository = rewardPointEntryRepository;
    }

    @Override
    public RewardSummaryDTO getRewardSummary(Integer mentorId) {
        Integer totalPoints = rewardPointEntryRepository.getTotalPointsByMentorId(mentorId);
        Integer sessionPoints = rewardPointEntryRepository.getPointsByMentorIdAndType(
                mentorId, ContributionType.SESSION);
        Integer suggestionPoints = rewardPointEntryRepository.getPointsByMentorIdAndType(
                mentorId, ContributionType.CURRICULUM_SUGGESTION);

        List<RewardPointResponseDTO> history = rewardPointEntryRepository
                .findByMentorIdOrderByUpdateDateDesc(mentorId)
                .stream().map(this::mapToResponseDTO).collect(Collectors.toList());

        RewardSummaryDTO summary = new RewardSummaryDTO();
        summary.setMentorId(mentorId);
        summary.setTotalRewardPoints(totalPoints);
        summary.setSessionPoints(sessionPoints);
        summary.setSuggestionPoints(suggestionPoints);
        summary.setContributionHistory(history);

        return summary;
    }

    private RewardPointResponseDTO mapToResponseDTO(RewardPointEntry entry) {
        return RewardPointResponseDTO.builder()
                .id(entry.getId())
                .mentorId(entry.getMentorId())
                .contributionType(entry.getContributionType().name())
                .pointsAwarded(entry.getPointsAwarded())
                .contributionReferenceId(entry.getContributionReferenceId())
                .updatedTotalPoints(entry.getUpdatedTotalPoints())
                .updateDate(entry.getUpdateDate())
                .build();
    }
}
