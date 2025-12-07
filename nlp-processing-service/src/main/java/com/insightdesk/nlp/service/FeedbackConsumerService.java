package com.insightdesk.nlp.service;

import com.insightdesk.nlp.dto.FeedbackEvent;
import com.insightdesk.nlp.entity.Feedback;
import com.insightdesk.nlp.repository.FeedbackRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class FeedbackConsumerService {

    private static final Logger logger = LoggerFactory.getLogger(FeedbackConsumerService.class);

    @Autowired
    private FeedbackRepository feedbackRepository;

    @Autowired
    private NlpProcessingService nlpProcessingService;

    /**
     * Consumes feedback events from Kafka topic
     */
    @KafkaListener(
        topics = "${feedback.kafka.topic}",
        groupId = "${spring.kafka.consumer.group-id}",
        containerFactory = "kafkaListenerContainerFactory"
    )
    @Transactional
    public void consumeFeedback(
            @Payload FeedbackEvent feedbackEvent,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment acknowledgment) {
        
        try {
            logger.info("Received feedback event: id={}, partition={}, offset={}", 
                feedbackEvent.getId(), partition, offset);

            // Check if feedback exists and is already processed (idempotency)
            Optional<Feedback> existingFeedback = feedbackRepository.findById(feedbackEvent.getId());
            if (existingFeedback.isPresent() && Boolean.TRUE.equals(existingFeedback.get().getProcessed())) {
                logger.info("Feedback {} already processed. Skipping.", feedbackEvent.getId());
                acknowledgment.acknowledge();
                return;
            }

            // Process the feedback with NLP
            Feedback processedFeedback = nlpProcessingService.processFeedback(feedbackEvent);
            
            // Save/update in database
            feedbackRepository.save(processedFeedback);
            
            logger.info("Successfully processed and updated feedback: id={}, category={}, processed={}", 
                processedFeedback.getId(), processedFeedback.getCategory(), processedFeedback.getProcessed());

            // Acknowledge message after successful processing
            acknowledgment.acknowledge();
            
        } catch (Exception e) {
            logger.error("Error processing feedback: id={}, error={}", 
                feedbackEvent.getId(), e.getMessage(), e);
            
            // For now, acknowledge to avoid infinite retries
            // In production, you might want to implement DLQ (Dead Letter Queue)
            acknowledgment.acknowledge();
        }
    }
}
