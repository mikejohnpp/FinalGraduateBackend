package org.social.common.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Objects;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class DismissedSuggestionId implements Serializable {

    @Column(name = "user_id")
    private Integer userId;

    @Column(name = "dismissed_user_id")
    private Integer dismissedUserId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DismissedSuggestionId that = (DismissedSuggestionId) o;
        return Objects.equals(userId, that.userId) && Objects.equals(dismissedUserId, that.dismissedUserId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, dismissedUserId);
    }
}
