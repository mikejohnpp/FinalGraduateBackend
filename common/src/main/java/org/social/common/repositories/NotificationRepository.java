package org.social.common.repositories;

import org.social.common.entities.Notification;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    /**
     * Danh sách thông báo (tất cả), mới nhất trước, phân trang bằng cursor
     * createdAt
     */
    @Query("SELECT n FROM Notification n JOIN FETCH n.recipient LEFT JOIN FETCH n.actor "
            + "WHERE n.recipient.id = :userId AND n.createdAt < :cursor ORDER BY n.createdAt DESC")
    List<Notification> findByRecipientBefore(@Param("userId") Integer userId,
            @Param("cursor") Instant cursor,
            Pageable pageable);

    /**
     * Danh sách thông báo chưa đọc, mới nhất trước, phân trang bằng cursor
     * createdAt
     */
    @Query("SELECT n FROM Notification n JOIN FETCH n.recipient LEFT JOIN FETCH n.actor "
            + "WHERE n.recipient.id = :userId AND n.isRead = false AND n.createdAt < :cursor ORDER BY n.createdAt DESC")
    List<Notification> findUnreadByRecipientBefore(@Param("userId") Integer userId,
            @Param("cursor") Instant cursor,
            Pageable pageable);

    /** Đếm số thông báo chưa đọc */
    @Query("SELECT COUNT(n) FROM Notification n WHERE n.recipient.id = :userId AND n.isRead = false")
    long countUnread(@Param("userId") Integer userId);

    /** Đánh dấu 1 thông báo đã đọc (chỉ khi thuộc về user) */
    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true, n.readAt = :now "
            + "WHERE n.id = :id AND n.recipient.id = :userId AND n.isRead = false")
    int markAsRead(@Param("id") Long id, @Param("userId") Integer userId, @Param("now") Instant now);

    /** Đánh dấu tất cả thông báo của user là đã đọc */
    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true, n.readAt = :now "
            + "WHERE n.recipient.id = :userId AND n.isRead = false")
    int markAllAsRead(@Param("userId") Integer userId, @Param("now") Instant now);
}
