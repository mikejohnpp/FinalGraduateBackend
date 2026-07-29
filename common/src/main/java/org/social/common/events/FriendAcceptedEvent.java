package org.social.common.events;

/**
 * Sự kiện phát ra khi hai người dùng trở thành bạn bè (lời mời kết bạn được
 * chấp nhận). Được publish bởi user-service và tiêu thụ bởi chat-service để
 * tạo sẵn cuộc trò chuyện 1-1 giữa hai người.
 *
 * @param userAId một trong hai người dùng
 * @param userBId người dùng còn lại
 */
public record FriendAcceptedEvent(
        Integer userAId,
        Integer userBId) {
}
