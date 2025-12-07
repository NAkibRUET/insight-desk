package com.insightdesk.nlp.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * DTO representing feedback event consumed from Kafka
 */
public class FeedbackEvent {
    
    private Long id;
    private Long userId;
    private Long projectId;
    private String message;
    private String category;
    private BigDecimal sentimentScore;
    private String[] keywords;
    private Object entities;  // Can be String[] or Map for JSONB
    private Boolean processed;
    
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    // Constructors
    public FeedbackEvent() {}

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public BigDecimal getSentimentScore() {
        return sentimentScore;
    }

    public void setSentimentScore(BigDecimal sentimentScore) {
        this.sentimentScore = sentimentScore;
    }

    public String[] getKeywords() {
        return keywords;
    }

    public void setKeywords(String[] keywords) {
        this.keywords = keywords;
    }

    public Object getEntities() {
        return entities;
    }

    public void setEntities(Object entities) {
        this.entities = entities;
    }

    public Boolean getProcessed() {
        return processed;
    }

    public void setProcessed(Boolean processed) {
        this.processed = processed;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(OffsetDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() {
        return "FeedbackEvent{" +
                "id=" + id +
                ", userId=" + userId +
                ", projectId=" + projectId +
                ", message='" + message + '\'' +
                ", category='" + category + '\'' +
                ", sentimentScore=" + sentimentScore +
                ", processed=" + processed +
                ", createdAt=" + createdAt +
                '}';
    }
}
