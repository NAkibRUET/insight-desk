package com.insightdesk.nlp.service;

import com.insightdesk.nlp.dto.FeedbackEvent;
import com.insightdesk.nlp.entity.Feedback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Service for processing feedback with NLP
 * Currently just simulates processing - actual NLP will be added later
 */
@Service
public class NlpProcessingService {

    private static final Logger logger = LoggerFactory.getLogger(NlpProcessingService.class);

    /**
     * Process feedback event and update with NLP results
     * For now, this is a simple pass-through with logging
     * Real NLP processing will be added later
     */
    public Feedback processFeedback(FeedbackEvent event) {
        logger.info("Processing feedback: id={}, message={}", event.getId(), event.getMessage());

        Feedback feedback = new Feedback();
        feedback.setId(event.getId());
        feedback.setUserId(event.getUserId());
        feedback.setProjectId(event.getProjectId());
        feedback.setMessage(event.getMessage());
        
        // Determine category based on sentiment
        String category = determineSentimentLabel(event.getSentimentScore());
        feedback.setCategory(category);
        
        // Set or update sentiment score
        BigDecimal sentimentScore = event.getSentimentScore();
        if (sentimentScore == null) {
            // Simulate sentiment analysis if not present
            sentimentScore = simulateSentimentScore(event.getMessage());
        }
        feedback.setSentimentScore(sentimentScore);
        
        // Process keywords
        String[] keywords = event.getKeywords();
        if (keywords == null || keywords.length == 0) {
            keywords = extractKeywords(event.getMessage());
        }
        feedback.setKeywords(keywords);
        
        // Process entities
        Map<String, Object> entities = convertEntitiesToMap(event.getEntities());
        feedback.setEntities(entities);
        
        // Mark as processed
        feedback.setProcessed(true);
        feedback.setUpdatedAt(OffsetDateTime.now());

        logger.info("Completed processing for feedback: id={}, category={}, score={}", 
            event.getId(), category, sentimentScore);
        
        return feedback;
    }

    /**
     * Determine sentiment label based on score
     */
    private String determineSentimentLabel(BigDecimal score) {
        if (score == null) {
            return "neutral";
        }
        
        if (score.compareTo(new BigDecimal("0.6")) > 0) {
            return "positive";
        } else if (score.compareTo(new BigDecimal("0.4")) < 0) {
            return "negative";
        } else {
            return "neutral";
        }
    }

    /**
     * Simulate sentiment score analysis (to be replaced with real NLP)
     */
    private BigDecimal simulateSentimentScore(String message) {
        if (message == null) {
            return new BigDecimal("0.50");
        }
        
        String lowerMessage = message.toLowerCase();
        if (lowerMessage.contains("great") || lowerMessage.contains("excellent") || 
            lowerMessage.contains("amazing") || lowerMessage.contains("love")) {
            return new BigDecimal("0.85");
        } else if (lowerMessage.contains("bad") || lowerMessage.contains("terrible") || 
                   lowerMessage.contains("hate") || lowerMessage.contains("poor")) {
            return new BigDecimal("0.25");
        }
        
        return new BigDecimal("0.50");
    }

    /**
     * Extract keywords from message (simple implementation)
     */
    private String[] extractKeywords(String message) {
        if (message == null || message.isEmpty()) {
            return new String[]{};
        }
        
        // Simple keyword extraction - to be replaced with real NLP
        String[] words = message.toLowerCase()
            .replaceAll("[^a-z0-9\\s]", "")
            .split("\\s+");
        
        // Filter out common words (very basic stop words)
        java.util.List<String> keywords = new java.util.ArrayList<>();
        for (String word : words) {
            if (word.length() > 3 && 
                !word.matches("the|and|for|with|this|that|from|have|been")) {
                keywords.add(word);
                if (keywords.size() >= 5) break;
            }
        }
        
        return keywords.toArray(new String[0]);
    }

    /**
     * Convert entities to JSONB map
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> convertEntitiesToMap(Object entities) {
        Map<String, Object> entityMap = new HashMap<>();
        
        if (entities != null) {
            if (entities instanceof String[]) {
                String[] entitiesArray = (String[]) entities;
                if (entitiesArray.length > 0) {
                    entityMap.put("items", entitiesArray);
                    entityMap.put("count", entitiesArray.length);
                }
            } else if (entities instanceof Map) {
                entityMap = (Map<String, Object>) entities;
            } else if (entities instanceof java.util.List) {
                entityMap.put("items", entities);
                entityMap.put("count", ((java.util.List<?>) entities).size());
            }
        }
        
        return entityMap;
    }
}
