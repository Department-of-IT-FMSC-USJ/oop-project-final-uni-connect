package com.uniconnect.industry.modules.suggestions.repository;

import com.uniconnect.industry.modules.suggestions.entity.CurriculumSuggestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CurriculumSuggestionRepository extends JpaRepository<CurriculumSuggestion, Integer> {

    List<CurriculumSuggestion> findByMentorIdOrderBySubmissionDateDesc(Integer mentorId);

    boolean existsByMentorIdAndSuggestionTitle(Integer mentorId, String suggestionTitle);
}
