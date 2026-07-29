package org.social.common.entities;

public enum NotificationType {
    COMMENT, // Ai đó bình luận vào bài viết của bạn
    REPLY, // Ai đó trả lời bình luận của bạn
    FRIEND_REQUEST, // Ai đó gửi lời mời kết bạn
    FRIEND_ACCEPT, // Ai đó chấp nhận lời mời kết bạn
    GROUP_JOIN_REQUEST, // Có yêu cầu tham gia nhóm (gửi cho admin/owner)
    GROUP_JOIN_APPROVED, // Yêu cầu tham gia nhóm được duyệt
    GROUP_POST_PENDING, // Có bài viết chờ duyệt trong nhóm (gửi cho admin/owner)
    GROUP_POST_APPROVED // Bài viết trong nhóm được duyệt
}
