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
@Table(name = "storys", schema = "FinalGraduateDB")
public class Story {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Size(max = 500)
    @Column(name = "content", nullable = true, length = 500)
    private String content;

    @Size(max = 1000)
    @Column(name = "url_image", nullable = true, length = 1000)
    private String urlImage;

    @Size(max=1000)
    @Column(name = "url_video", nullable = true, length = 1000)
    private String urlVideo;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "created_at")
    private Instant createdAt;

    @ColumnDefault("1")
    @Column(name = "is_active")
    private Boolean isActive;

    @Column(name ="type")
    private String type;

    @Column(name ="color")
    private String color;


}
