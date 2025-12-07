package com.insightdesk.nlp.repository;

import com.insightdesk.nlp.entity.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FeedbackRepository extends JpaRepository<Feedback, Long> {
    
    /**
     * Find unprocessed feedback entries
     */
    @Query("SELECT f FROM Feedback f WHERE f.processed = false")
    List<Feedback> findUnprocessedFeedback();
}
