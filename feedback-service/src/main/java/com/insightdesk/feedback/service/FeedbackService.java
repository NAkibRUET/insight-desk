package com.insightdesk.feedback.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.insightdesk.feedback.dto.FeedbackRequest;
import com.insightdesk.feedback.dto.FeedbackResponse;
import com.insightdesk.feedback.entity.Feedback;
import com.insightdesk.feedback.repository.FeedbackRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class FeedbackService {

    @Autowired
    private FeedbackRepository feedbackRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private KafkaProducerService kafkaProducerService;

    /**
     * Create a single feedback entry
     */
    @Transactional
    public FeedbackResponse createFeedback(FeedbackRequest request) {
        Feedback feedback = new Feedback();
        feedback.setUserId(request.getUserId());
        feedback.setProjectId(request.getProjectId());
        feedback.setMessage(request.getMessage());
        feedback.setCategory(request.getCategory());
        feedback.setSentimentScore(request.getSentimentScore());
        feedback.setKeywords(request.getKeywords());
        feedback.setEntities(request.getEntities());
        feedback.setProcessed(false);

        Feedback savedFeedback = feedbackRepository.save(feedback);
        FeedbackResponse response = mapToResponse(savedFeedback);
        
        // Send feedback event to Kafka
        kafkaProducerService.sendFeedbackEvent(response);
        
        return response;
    }

    /**
     * Import feedback from CSV file
     * Expected CSV format: userId,projectId,message,category,sentimentScore,keywords
     * Example: 1,100,"Great product","positive",0.95,"quality,excellent,satisfied"
     */
    @Transactional
    public List<FeedbackResponse> importFromCsv(MultipartFile file) throws Exception {
        List<FeedbackResponse> responses = new ArrayList<>();
        
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            String line;
            boolean isHeader = true;
            
            while ((line = reader.readLine()) != null) {
                // Skip header line
                if (isHeader) {
                    isHeader = false;
                    continue;
                }
                
                String[] fields = parseCsvLine(line);
                
                if (fields.length >= 3) { // At minimum we need userId, projectId, and message
                    Feedback feedback = new Feedback();
                    
                    // Parse userId (optional)
                    if (!fields[0].trim().isEmpty()) {
                        feedback.setUserId(Long.parseLong(fields[0].trim()));
                    }
                    
                    // Parse projectId (optional)
                    if (fields.length > 1 && !fields[1].trim().isEmpty()) {
                        feedback.setProjectId(Long.parseLong(fields[1].trim()));
                    }
                    
                    // Parse message (required)
                    if (fields.length > 2) {
                        feedback.setMessage(fields[2].trim());
                    }
                    
                    // Parse category (optional)
                    if (fields.length > 3 && !fields[3].trim().isEmpty()) {
                        feedback.setCategory(fields[3].trim());
                    }
                    
                    // Parse sentimentScore (optional)
                    if (fields.length > 4 && !fields[4].trim().isEmpty()) {
                        feedback.setSentimentScore(new BigDecimal(fields[4].trim()));
                    }
                    
                    // Parse keywords (optional)
                    if (fields.length > 5 && !fields[5].trim().isEmpty()) {
                        String[] keywords = fields[5].trim().split(",");
                        feedback.setKeywords(keywords);
                    }
                    
                    feedback.setProcessed(false);
                    Feedback savedFeedback = feedbackRepository.save(feedback);
                    FeedbackResponse response = mapToResponse(savedFeedback);
                    responses.add(response);
                    
                    // Send feedback event to Kafka
                    kafkaProducerService.sendFeedbackEvent(response);
                }
            }
        }
        
        return responses;
    }

    /**
     * Import feedback from JSON file
     * Expected JSON format: Array of feedback objects
     * [{"userId": 1, "projectId": 100, "message": "Great product", "category": "positive", ...}, ...]
     */
    @Transactional
    public List<FeedbackResponse> importFromJson(MultipartFile file) throws Exception {
        List<FeedbackResponse> responses = new ArrayList<>();
        
        // Parse JSON array
        FeedbackRequest[] feedbackRequests = objectMapper.readValue(
            file.getInputStream(), 
            FeedbackRequest[].class
        );
        
        // Save each feedback
        for (FeedbackRequest request : feedbackRequests) {
            Feedback feedback = new Feedback();
            feedback.setUserId(request.getUserId());
            feedback.setProjectId(request.getProjectId());
            feedback.setMessage(request.getMessage());
            feedback.setCategory(request.getCategory());
            feedback.setSentimentScore(request.getSentimentScore());
            feedback.setKeywords(request.getKeywords());
            feedback.setEntities(request.getEntities());
            feedback.setProcessed(false);
            
            Feedback savedFeedback = feedbackRepository.save(feedback);
            FeedbackResponse response = mapToResponse(savedFeedback);
            responses.add(response);
            
            // Send feedback event to Kafka
            kafkaProducerService.sendFeedbackEvent(response);
        }
        
        return responses;
    }

    /**
     * Get all feedback entries
     */
    public List<FeedbackResponse> getAllFeedback() {
        List<Feedback> feedbackList = feedbackRepository.findAll();
        List<FeedbackResponse> responses = new ArrayList<>();
        
        for (Feedback feedback : feedbackList) {
            responses.add(mapToResponse(feedback));
        }
        
        return responses;
    }

    /**
     * Get feedback by ID
     */
    public FeedbackResponse getFeedbackById(Long id) {
        Feedback feedback = feedbackRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Feedback not found with id: " + id));
        return mapToResponse(feedback);
    }

    /**
     * Helper method to parse CSV line considering quoted fields
     */
    private String[] parseCsvLine(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder currentField = new StringBuilder();
        boolean inQuotes = false;
        
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                fields.add(currentField.toString());
                currentField = new StringBuilder();
            } else {
                currentField.append(c);
            }
        }
        
        fields.add(currentField.toString());
        return fields.toArray(new String[0]);
    }

    /**
     * Map entity to response DTO
     */
    private FeedbackResponse mapToResponse(Feedback feedback) {
        FeedbackResponse response = new FeedbackResponse();
        response.setId(feedback.getId());
        response.setUserId(feedback.getUserId());
        response.setProjectId(feedback.getProjectId());
        response.setMessage(feedback.getMessage());
        response.setCategory(feedback.getCategory());
        response.setSentimentScore(feedback.getSentimentScore());
        response.setKeywords(feedback.getKeywords());
        response.setEntities(feedback.getEntities());
        response.setProcessed(feedback.getProcessed());
        response.setCreatedAt(feedback.getCreatedAt());
        response.setUpdatedAt(feedback.getUpdatedAt());
        return response;
    }
}
