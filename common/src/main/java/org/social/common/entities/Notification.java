package org.social.common.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "notifications", schema = "FinalGraduateDB")
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    /** Người nhận thông báo */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "recipient_id", nullable = false)
    private User recipient;

    /** Người tạo ra hành động (có thể null với thông báo hệ thống) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actor_id")
    private User actor;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 40)
    private NotificationType type;

    /** Loại thực thể liên quan: POST, COMMENT, FRIEND, GROUP... */
    @Column(name = "entity_type", length = 20)
    private String entityType;

    /** Id của thực thể liên quan (postId, commentId, groupId...) */
    @Column(name = "entity_id")
    private Integer entityId;

    /** Nội dung hiển thị sẵn (tuỳ chọn) */
    @Column(name = "message", length = 500)
    private String message;

    /** Đường dẫn điều hướng khi bấm vào thông báo */
    @Column(name = "link", length = 500)
    private String link;

    @ColumnDefault("0")
    @Column(name = "is_read", nullable = false)
    private Boolean isRead;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "read_at")
    private Instant readAt;
}
