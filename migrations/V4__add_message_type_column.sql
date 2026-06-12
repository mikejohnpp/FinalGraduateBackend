ALTER TABLE FinalGraduateDB.messages
    ADD COLUMN message_type VARCHAR(20) NOT NULL DEFAULT 'TEXT',
    ADD COLUMN call_duration INT NULL;
