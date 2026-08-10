CREATE TABLE IF NOT EXISTS dismissed_suggestions (
    user_id           INT NOT NULL,
    dismissed_user_id INT NOT NULL,
    created_at        TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id, dismissed_user_id),
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (dismissed_user_id) REFERENCES users(id)
);
