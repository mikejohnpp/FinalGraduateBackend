-- V9: Notification system — stores user notifications (comment, reply, friend request/accept, group events)

CREATE TABLE
IF NOT EXISTS `notifications`
(
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
  `recipient_id` INT NOT NULL,
  `actor_id` INT NULL,
  `type` VARCHAR
(40) NOT NULL,
  `entity_type` VARCHAR
(20) NULL,
  `entity_id` INT NULL,
  `message` VARCHAR
(500) NULL,
  `link` VARCHAR
(500) NULL,
  `is_read` TINYINT
(1) NOT NULL DEFAULT 0,
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `read_at` TIMESTAMP NULL,
  CONSTRAINT `fk_notification_recipient` FOREIGN KEY
(`recipient_id`) REFERENCES `users`
(`id`),
  CONSTRAINT `fk_notification_actor` FOREIGN KEY
(`actor_id`) REFERENCES `users`
(`id`)
);

-- List a user's notifications newest-first + cursor pagination on created_at
CREATE INDEX `idx_notification_recipient_created` ON `notifications`
(`recipient_id`, `created_at`);

-- Fast unread-count query
CREATE INDEX `idx_notification_recipient_unread` ON `notifications`
(`recipient_id`, `is_read`);
