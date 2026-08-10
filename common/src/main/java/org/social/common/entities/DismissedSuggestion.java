package org.social.common.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "dismissed_suggestions", schema = "FinalGraduateDB")
public class DismissedSuggestion {

    @EmbeddedId
    private DismissedSuggestionId id;

    @MapsId("userId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @MapsId("dismissedUserId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "dismissed_user_id", nullable = false)
    private User dismissedUser;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}
