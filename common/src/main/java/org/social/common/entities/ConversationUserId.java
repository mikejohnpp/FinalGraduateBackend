package org.social.common.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@EqualsAndHashCode
@Embeddable
public class ConversationUserId implements Serializable {
    private static final long serialVersionUID = 4423912483003909409L;
    @NotNull
    @Column(name = "conversation_id", nullable = false)
    private Integer conversationId;

    @NotNull
    @Column(name = "user_id", nullable = false)
    private Integer userId;


}