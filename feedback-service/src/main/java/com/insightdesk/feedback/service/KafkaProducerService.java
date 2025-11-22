package com.insightdesk.feedback.service;

import com.insightdesk.feedback.dto.FeedbackResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
public class KafkaProducerService {

    private static final Logger logger = LoggerFactory.getLogger(KafkaProducerService.class);

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${feedback.kafka.topic}")
    private String feedbackTopic;

    /**
     * Send feedback event to Kafka
     */
    public void sendFeedbackEvent(FeedbackResponse feedback) {
        try {
            CompletableFuture<SendResult<String, Object>> future = 
                kafkaTemplate.send(feedbackTopic, feedback.getId().toString(), feedback);
            
            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    logger.info("Sent feedback event: [id={}] with offset=[{}]", 
                        feedback.getId(), 
                        result.getRecordMetadata().offset());
                } else {
                    logger.error("Unable to send feedback event: [id={}] due to: {}", 
                        feedback.getId(), 
                        ex.getMessage());
                }
            });
        } catch (Exception ex) {
            logger.error("Error sending feedback event: {}", ex.getMessage(), ex);
        }
    }
}
