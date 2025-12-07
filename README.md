# Insight Desk - Feedback Analysis Platform

A microservices-based platform for collecting and analyzing user feedback using Kafka for asynchronous processing and NLP for sentiment analysis.

## Architecture

- **feedback-service** (Port 8080): Collects feedback via REST API and publishes to Kafka
- **nlp-processing-service** (Port 8081): Consumes feedback events and performs NLP analysis
- **Kafka**: Message broker for asynchronous communication
- **PostgreSQL**: Shared database for both services

## Prerequisites

- Java 21
- Docker & Docker Compose
- PostgreSQL client (for running SQL scripts)
- Gradle (included via wrapper)

## Quick Start

### 1. Start Infrastructure (Kafka & PostgreSQL)

```bash
docker-compose up -d
```

### 2. Setup Database

```bash
psql -U postgres -d insight_desk -f feedback-service/src/scripts/feedback.sql
```

### 3. Start Services

**Terminal 1 - Feedback Service:**
```bash
cd feedback-service
.\gradlew bootRun
```

**Terminal 2 - NLP Processing Service:**
```bash
cd nlp-processing-service
.\gradlew bootRun
```

### 4. Verify Health

```bash
curl http://localhost:8080/health  # Feedback Service
curl http://localhost:8081/health  # NLP Service
```

## Running the Application

### Using Gradle Wrapper (Windows):

```bash
.\gradlew.bat bootRun
```

### Using Gradle Wrapper (Linux/Mac):

```bash
./gradlew bootRun
```

The application will start on `http://localhost:8080`

### 1. Web Form

Open your browser and navigate to:
```
http://localhost:8080/index.html
```

Fill out the feedback form and submit.

### Successful Feedback Submission:
```json
{
  "id": 1,
  "userId": 1,
  "projectId": 100,
  "message": "Great product!",
  "category": "praise",
  "sentimentScore": 0.95,
  "keywords": ["quality", "excellent"],
  "entities": null,
  "processed": false,
  "createdAt": "2025-11-21T10:30:00Z",
  "updatedAt": "2025-11-21T10:30:00Z"
}
```

### CSV/JSON Import Success:
```json
{
  "message": "Successfully imported 5 feedback entries",
  "count": 5,
  "data": [...]
}
```

## Testing with cURL

### Submit feedback:
```bash
curl -X POST http://localhost:8080/api/feedback \
  -H "Content-Type: application/json" \
  -d "{\"message\":\"Test feedback\",\"category\":\"other\"}"
```

### Import CSV:
```bash
curl -X POST http://localhost:8080/api/feedback/import/csv \
  -F "file=@sample_feedback.csv"
```

### Import JSON:
```bash
curl -X POST http://localhost:8080/api/feedback/import/json \
  -F "file=@sample_feedback.json"
```

## Technologies Used

- **Spring Boot 3.5.7** - Application framework
- **Spring Data JPA** - Database operations
- **PostgreSQL** - Database with full-text search
- **Jackson** - JSON processing
- **Gradle** - Build tool

## Notes

- The `message_tsv` column is automatically populated by a PostgreSQL trigger for full-text search
- Timestamps (`created_at`, `updated_at`) are managed automatically
- The application supports CORS for public form access
- File uploads are limited to 10MB

## Checking Kafka console
```
docker exec -it kafka kafka-console-consumer --bootstrap-server localhost:9092 --topic feedback-events --from-beginning
```