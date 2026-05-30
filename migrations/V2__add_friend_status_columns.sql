-- Migration: Add status, created_at, updated_at to user_friends
-- Run manually against FinalGraduateDB

ALTER TABLE `user_friends`
    ADD COLUMN `status`     VARCHAR(20)  NOT NULL DEFAULT 'ACCEPTED' AFTER `friend_id`,
    ADD COLUMN `created_at` TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP AFTER `status`,
    ADD COLUMN `updated_at` TIMESTAMP    NULL     DEFAULT NULL AFTER `created_at`;

-- Performance indexes
CREATE INDEX idx_user_friends_user_status   ON user_friends (user_id, status);
CREATE INDEX idx_user_friends_friend_status ON user_friends (friend_id, status);
