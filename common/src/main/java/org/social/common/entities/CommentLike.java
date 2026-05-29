package org.social.common.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "comment_likes", schema = "FinalGraduateDB")
@IdClass(CommentLikeId.class)
public class CommentLike {

    @Id
    @Column(name = "comment_id", nullable = false)
    private Integer commentId;

    @Id
    @Column(name = "user_id", nullable = false)
    private Integer userId;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "created_at")
    private Instant createdAt;
}
