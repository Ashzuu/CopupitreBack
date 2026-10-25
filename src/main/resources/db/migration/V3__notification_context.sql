SET search_path TO main;

-- Ajout du contexte de conversation aux notifications internes (US-COM-02, US-COM-03)
ALTER TABLE notifications ADD COLUMN IF NOT EXISTS context_type VARCHAR(50);
ALTER TABLE notifications ADD COLUMN IF NOT EXISTS context_id BIGINT;
