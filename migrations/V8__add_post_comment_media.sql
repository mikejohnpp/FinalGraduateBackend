-- V8: Multi-media support for posts and comments — stores media links (IMAGE, VIDEO, AUDIO, FILE)

CREATE TABLE IF NOT EXISTS `post_media` (
  `id` INT AUTO_INCREMENT PRIMARY KEY,
  `post_id` INT NOT NULL,
  `url` VARCHAR(1000) NOT NULL,
  `media_type` VARCHAR(20) NOT NULL DEFAULT 'IMAGE',
  `position` INT NOT NULL DEFAULT 0,
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT `fk_post_media_post` FOREIGN KEY (`post_id`) REFERENCES `posts` (`id`)
);

CREATE INDEX `idx_post_media_post_id` ON `post_media` (`post_id`);

CREATE TABLE IF NOT EXISTS `comment_media` (
  `id` INT AUTO_INCREMENT PRIMARY KEY,
  `comment_id` INT NOT NULL,
  `url` VARCHAR(1000) NOT NULL,
  `media_type` VARCHAR(20) NOT NULL DEFAULT 'IMAGE',
  `position` INT NOT NULL DEFAULT 0,
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT `fk_comment_media_comment` FOREIGN KEY (`comment_id`) REFERENCES `comments` (`id`)
);

CREATE INDEX `idx_comment_media_comment_id` ON `comment_media` (`comment_id`);
