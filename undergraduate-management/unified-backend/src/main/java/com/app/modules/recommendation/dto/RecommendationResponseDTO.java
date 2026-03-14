package com.app.modules.recommendation.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO for returning a mentor recommendation to the frontend.
 */
public class RecommendationResponseDTO {

    private Integer recommendationId;
    private Integer studentId;
    private Integer mentorId;
    private String mentorName;
    private String mentorCategory;
    private String expertiseArea;
    private List<String> matchingTags;
    private Integer matchScore;
    private LocalDateTime recommendationDate;

    public RecommendationResponseDTO() {
    }

    public RecommendationResponseDTO(Integer recommendationId, Integer studentId, Integer mentorId,
                                      String mentorName, String mentorCategory, String expertiseArea,
                                      List<String> matchingTags, Integer matchScore,
                                      LocalDateTime recommendationDate) {
        this.recommendationId = recommendationId;
        this.studentId = studentId;
        this.mentorId = mentorId;
        this.mentorName = mentorName;
        this.mentorCategory = mentorCategory;
        this.expertiseArea = expertiseArea;
        this.matchingTags = matchingTags;
        this.matchScore = matchScore;
        this.recommendationDate = recommendationDate;
    }

    // Getters and Setters

    public Integer getRecommendationId() {
        return recommendationId;
    }

    public void setRecommendationId(Integer recommendationId) {
        this.recommendationId = recommendationId;
    }

    public Integer getStudentId() {
        return studentId;
    }

    public void setStudentId(Integer studentId) {
        this.studentId = studentId;
    }

    public Integer getMentorId() {
        return mentorId;
    }

    public void setMentorId(Integer mentorId) {
        this.mentorId = mentorId;
    }

    public String getMentorName() {
        return mentorName;
    }

    public void setMentorName(String mentorName) {
        this.mentorName = mentorName;
    }

    public String getMentorCategory() {
        return mentorCategory;
    }

    public void setMentorCategory(String mentorCategory) {
        this.mentorCategory = mentorCategory;
    }

    public String getExpertiseArea() {
        return expertiseArea;
    }

    public void setExpertiseArea(String expertiseArea) {
        this.expertiseArea = expertiseArea;
    }

    public List<String> getMatchingTags() {
        return matchingTags;
    }

    public void setMatchingTags(List<String> matchingTags) {
        this.matchingTags = matchingTags;
    }

    public Integer getMatchScore() {
        return matchScore;
    }

    public void setMatchScore(Integer matchScore) {
        this.matchScore = matchScore;
    }

    public LocalDateTime getRecommendationDate() {
        return recommendationDate;
    }

    public void setRecommendationDate(LocalDateTime recommendationDate) {
        this.recommendationDate = recommendationDate;
    }

    @Override
    public String toString() {
        return "RecommendationResponseDTO{" +
                "recommendationId=" + recommendationId +
                ", studentId=" + studentId +
                ", mentorId=" + mentorId +
                ", mentorName='" + mentorName + '\'' +
                ", mentorCategory='" + mentorCategory + '\'' +
                ", expertiseArea='" + expertiseArea + '\'' +
                ", matchingTags=" + matchingTags +
                ", matchScore=" + matchScore +
                ", recommendationDate=" + recommendationDate +
                '}';
    }

    // Static Builder

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Integer recommendationId;
        private Integer studentId;
        private Integer mentorId;
        private String mentorName;
        private String mentorCategory;
        private String expertiseArea;
        private List<String> matchingTags;
        private Integer matchScore;
        private LocalDateTime recommendationDate;

        public Builder recommendationId(Integer recommendationId) {
            this.recommendationId = recommendationId;
            return this;
        }

        public Builder studentId(Integer studentId) {
            this.studentId = studentId;
            return this;
        }

        public Builder mentorId(Integer mentorId) {
            this.mentorId = mentorId;
            return this;
        }

        public Builder mentorName(String mentorName) {
            this.mentorName = mentorName;
            return this;
        }

        public Builder mentorCategory(String mentorCategory) {
            this.mentorCategory = mentorCategory;
            return this;
        }

        public Builder expertiseArea(String expertiseArea) {
            this.expertiseArea = expertiseArea;
            return this;
        }

        public Builder matchingTags(List<String> matchingTags) {
            this.matchingTags = matchingTags;
            return this;
        }

        public Builder matchScore(Integer matchScore) {
            this.matchScore = matchScore;
            return this;
        }

        public Builder recommendationDate(LocalDateTime recommendationDate) {
            this.recommendationDate = recommendationDate;
            return this;
        }

        public RecommendationResponseDTO build() {
            return new RecommendationResponseDTO(
                    recommendationId, studentId, mentorId, mentorName,
                    mentorCategory, expertiseArea, matchingTags,
                    matchScore, recommendationDate);
        }
    }
}
