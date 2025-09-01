-- Add feedback_email_sent column to visit table to prevent duplicate feedback emails
ALTER TABLE visit ADD COLUMN feedback_email_sent BOOLEAN NOT NULL DEFAULT FALSE;

-- Update any existing COMPLETED visits to mark feedback as already sent (to prevent spam)
UPDATE visit SET feedback_email_sent = TRUE WHERE status = 'COMPLETED';
