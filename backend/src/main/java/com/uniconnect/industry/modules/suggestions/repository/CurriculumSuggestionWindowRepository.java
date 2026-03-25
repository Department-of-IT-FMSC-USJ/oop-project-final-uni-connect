package com.uniconnect.industry.modules.suggestions.repository;

import com.uniconnect.industry.modules.suggestions.entity.CurriculumSuggestionWindow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CurriculumSuggestionWindowRepository extends JpaRepository<CurriculumSuggestionWindow, Long> {
    Optional<CurriculumSuggestionWindow> findTopByOrderByUpdatedAtDesc();
}
