package com.insightdesk.feedback.controller;

import com.insightdesk.feedback.dto.FeedbackRequest;
import com.insightdesk.feedback.dto.FeedbackResponse;
import com.insightdesk.feedback.service.FeedbackService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/feedback")
@CrossOrigin(origins = "*") 
public class FeedbackController {

    @Autowired
    private FeedbackService feedbackService;

    @PostMapping
    public ResponseEntity<FeedbackResponse> submitFeedback(@RequestBody FeedbackRequest request) {
        try {
            FeedbackResponse response = feedbackService.createFeedback(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/import/csv")
    public ResponseEntity<Map<String, Object>> importCsv(@RequestParam("file") MultipartFile file) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            if (file.isEmpty()) {
                response.put("error", "File is empty");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
            
            if (!file.getOriginalFilename().endsWith(".csv")) {
                response.put("error", "Only CSV files are allowed");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
            
            List<FeedbackResponse> feedbackList = feedbackService.importFromCsv(file);
            response.put("message", "Successfully imported " + feedbackList.size() + " feedback entries");
            response.put("count", feedbackList.size());
            response.put("data", feedbackList);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            response.put("error", "Error importing CSV: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/import/json")
    public ResponseEntity<Map<String, Object>> importJson(@RequestParam("file") MultipartFile file) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            if (file.isEmpty()) {
                response.put("error", "File is empty");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
            
            if (!file.getOriginalFilename().endsWith(".json")) {
                response.put("error", "Only JSON files are allowed");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
            
            List<FeedbackResponse> feedbackList = feedbackService.importFromJson(file);
            response.put("message", "Successfully imported " + feedbackList.size() + " feedback entries");
            response.put("count", feedbackList.size());
            response.put("data", feedbackList);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            response.put("error", "Error importing JSON: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping
    public ResponseEntity<List<FeedbackResponse>> getAllFeedback() {
        try {
            List<FeedbackResponse> feedbackList = feedbackService.getAllFeedback();
            return ResponseEntity.ok(feedbackList);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<FeedbackResponse> getFeedbackById(@PathVariable Long id) {
        try {
            FeedbackResponse feedback = feedbackService.getFeedbackById(id);
            return ResponseEntity.ok(feedback);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> healthCheck() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "Feedback Service");
        return ResponseEntity.ok(response);
    }
}
