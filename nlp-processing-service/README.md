# NLP Processing Service

A Spring Boot microservice that consumes feedback events from Kafka and processes them for sentiment analysis and NLP insights.

## Features

- ✅ Kafka consumer for `feedback-events` topic
- ✅ Idempotent processing (no duplicate processing)
- ✅ PostgreSQL persistence for analysis results
- ✅ Health check endpoint
- ✅ Asynchronous processing with manual acknowledgment

## Setup

### Prerequisites

- Java 21
- Gradle
- PostgreSQL database
- Kafka (running via Docker)

### Database Setup

The service uses the existing `feedback` table. Make sure it's created:

```bash
psql -U postgres -d insight_desk -f ../feedback-service/src/scripts/feedback.sql
```

### Configuration

Edit `src/main/resources/application.properties`:

```properties
# Server port (different from feedback-service)
server.port=8081

# Database connection
spring.datasource.url=jdbc:postgresql://localhost:5432/insight_desk
spring.datasource.username=postgres
spring.datasource.password=Password1@

# Kafka consumer settings
spring.kafka.bootstrap-servers=localhost:9092
spring.kafka.consumer.group-id=nlp-processing-group
```

### Running the Service

```bash
./gradlew bootRun
```

Or build and run the JAR:

```bash
./gradlew build
java -jar build/libs/nlp-processing-service-0.0.1-SNAPSHOT.jar
```

## Endpoints

### Health Check
```bash
GET http://localhost:8081/health
```

Response:
```json
{
  "status": "UP",
  "service": "nlp-processing-service",
  "database": "UP",
  "timestamp": 1701234567890
}
```

## How It Works

1. **Kafka Consumer** listens to `feedback-events` topic
2. **Idempotency Check** ensures feedback is not processed twice (checks `processed` flag)
3. **NLP Processing** (currently simulated - real NLP to be added)
4. **Database Update** updates the existing `feedback` record with NLP results
5. **Manual Acknowledgment** ensures message is only marked as consumed after successful processing

## Database Schema

The service updates the existing `feedback` table:
- `category` - Updated with sentiment label (positive, negative, neutral)
- `sentiment_score` - Numeric score (0-1)
- `keywords` - Extracted keywords array (TEXT[])
- `entities` - Identified entities (JSONB)
- `processed` - Set to `true` after NLP processing
- `updated_at` - Timestamp of last update

## Architecture

```
```
feedback-service (port 8080)
    ↓ (produces event)
  Kafka (feedback-events topic)
    ↓ (consumes event)
nlp-processing-service (port 8081)
    ↓ (updates record)
  PostgreSQL (feedback table - sets processed=true)
```
## Next Steps

- Add actual NLP processing (sentiment analysis, keyword extraction)
- Implement Dead Letter Queue (DLQ) for failed messages
- Add metrics and monitoring
- Implement retry logic with exponential backoff
