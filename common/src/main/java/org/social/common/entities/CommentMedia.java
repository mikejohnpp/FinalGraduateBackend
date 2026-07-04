package org.social.common.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "comment_media", schema = "FinalGraduateDB")
public class CommentMedia {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "comment_id", nullable = false)
    private Comment comment;

    @NotNull
    @Size(max = 1000)
    @Column(name = "url", nullable = false, length = 1000)
    private String url;

    @Enumerated(EnumType.STRING)
    @ColumnDefault("'IMAGE'")
    @Column(name = "media_type", nullable = false, length = 20)
    private MediaType mediaType = MediaType.IMAGE;

    @ColumnDefault("0")
    @Column(name = "position", nullable = false)
    private Integer position;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "created_at")
    private Instant createdAt;
}
