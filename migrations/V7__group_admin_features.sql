-- V7: Group Admin features — description, createdAt for groups; join request workflow for user_group; post approval workflow

-- 1. groups table: add description and created_at
ALTER TABLE `groups` ADD COLUMN `description` TEXT DEFAULT NULL;
ALTER TABLE `groups` ADD COLUMN `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP;

-- 2. user_group table: add status and requested_at for join request workflow
ALTER TABLE `user_group` ADD COLUMN `status` VARCHAR(20) DEFAULT 'APPROVED';
ALTER TABLE `user_group` ADD COLUMN `requested_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP;

-- 3. posts table: add status for post approval workflow (group posts)
ALTER TABLE `posts` ADD COLUMN `status` VARCHAR(20) DEFAULT 'APPROVED';

-- 4. post_likes table: add created_at for weekly reaction stats
ALTER TABLE `post_likes` ADD COLUMN `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
