package com.insightdesk.feedback.repository;

import com.insightdesk.feedback.entity.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FeedbackRepository extends JpaRepository<Feedback, Long> {
    
    List<Feedback> findByUserId(Long userId);
    
    List<Feedback> findByProjectId(Long projectId);
    
    List<Feedback> findByCategory(String category);
    
    List<Feedback> findByProcessed(Boolean processed);
}
