-- Migration: Add user profile columns to users table
-- Run manually against FinalGraduateDB

ALTER TABLE `users`
    ADD COLUMN `cover_photo`  VARCHAR(500) NULL,
    ADD COLUMN `bio`          VARCHAR(101) NULL,
    ADD COLUMN `location`     VARCHAR(100) NULL,
    ADD COLUMN `education`    VARCHAR(200) NULL,
    ADD COLUMN `workplace`    VARCHAR(200) NULL,
    ADD COLUMN `hometown`     VARCHAR(100) NULL,
    ADD COLUMN `relationship` VARCHAR(50)  NULL,
    ADD COLUMN `gender`       VARCHAR(20)  NULL,
    ADD COLUMN `pronouns`     VARCHAR(50)  NULL,
    ADD COLUMN `language`     VARCHAR(50)  NULL;
