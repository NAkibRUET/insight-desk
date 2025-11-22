-- ======================================================
-- Feedback Table for AI/NLP Project
-- Includes:
--  - message_tsv for full-text search
--  - keywords (TEXT array)
--  - entities (JSONB)
--  - sentiment_score
--  - automatic trigger for tsvector update
-- ======================================================

-- 1. Create the table
CREATE TABLE IF NOT EXISTS feedback (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT,
    project_id BIGINT,
    message TEXT NOT NULL,
    category VARCHAR(50),
    sentiment_score NUMERIC(3,2) DEFAULT 0,
    keywords TEXT[],
    entities JSONB,
    processed BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW(),
    message_tsv tsvector
);

-- 2. Create GIN index on tsvector for fast full-text search
CREATE INDEX IF NOT EXISTS idx_feedback_message_tsv
ON feedback USING GIN (message_tsv);

-- 3. Function to update message_tsv automatically
CREATE OR REPLACE FUNCTION feedback_tsv_trigger()
RETURNS trigger AS $$
BEGIN
    -- Populate message_tsv column from message
    NEW.message_tsv := to_tsvector('english', COALESCE(NEW.message,''));
    
    -- Update updated_at timestamp automatically
    NEW.updated_at := NOW();
    
    RETURN NEW;
END
$$ LANGUAGE plpgsql;

-- 4. Trigger to run function BEFORE INSERT OR UPDATE
CREATE TRIGGER trg_feedback_tsv
BEFORE INSERT OR UPDATE OF message
ON feedback
FOR EACH ROW
EXECUTE FUNCTION feedback_tsv_trigger();