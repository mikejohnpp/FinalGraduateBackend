-- Alter comments table
ALTER TABLE comments
  ADD COLUMN parent_id INT NULL AFTER post_id,
  ADD COLUMN like_count INT DEFAULT 0,
  ADD COLUMN reply_count INT DEFAULT 0,
  ADD COLUMN updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  ADD CONSTRAINT fk_comment_parent FOREIGN KEY (parent_id) REFERENCES comments(id),
  ADD INDEX idx_comment_post_created (post_id, created_at DESC),
  ADD INDEX idx_comment_parent (parent_id, created_at ASC);

-- Alter posts table
ALTER TABLE posts
  ADD COLUMN comment_count INT DEFAULT 0,
  ADD COLUMN like_count INT DEFAULT 0;

-- Create comment_likes table
CREATE TABLE comment_likes (
  comment_id INT NOT NULL,
  user_id INT NOT NULL,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (comment_id, user_id),
  CONSTRAINT fk_comment_like_comment FOREIGN KEY (comment_id) REFERENCES comments(id),
  CONSTRAINT fk_comment_like_user FOREIGN KEY (user_id) REFERENCES users(id)
);
