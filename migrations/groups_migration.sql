-- Migration script for Group APIs

-- Update groups table
ALTER TABLE `groups` ADD COLUMN `cover_photo` VARCHAR(255) DEFAULT NULL;
ALTER TABLE `groups` ADD COLUMN `avatar` VARCHAR(255) DEFAULT NULL;
ALTER TABLE `groups` ADD COLUMN `privacy` VARCHAR(20) DEFAULT 'public';
ALTER TABLE `groups` ADD COLUMN `is_active` TINYINT(1) DEFAULT 1;

-- Update user_group table
ALTER TABLE `user_group` ADD COLUMN `role` VARCHAR(20) DEFAULT 'MEMBER';
