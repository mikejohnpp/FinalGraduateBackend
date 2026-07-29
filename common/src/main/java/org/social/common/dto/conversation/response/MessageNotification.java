package org.social.common.dto.conversation.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.social.common.entities.MessageType;

import java.time.Instant;

/**
 * Payload thông báo tin nhắn mới, gửi riêng cho từng thành viên của hội thoại
 * (trừ người gửi) qua destination /user/queue/messages.
 *
 * Khác với ChatMessageResponse (dùng để render trong khung chat), DTO này chỉ
 * mang đủ dữ liệu để client hiện toast/banner và điều hướng vào hội thoại.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MessageNotification {

    private Integer conversationId;

    private String conversationName;

    private Boolean isGroup;

    private Long messageId;

    private String content;

    private MessageType messageType;

    private Instant createdAt;

    private UserResponse sender;
}
