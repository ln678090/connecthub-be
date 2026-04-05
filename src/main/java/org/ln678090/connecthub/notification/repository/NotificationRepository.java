package org.ln678090.connecthub.notification.repository;

import org.ln678090.connecthub.notification.dto.resp.NotificationResp;
import org.ln678090.connecthub.notification.entity.Notification;

import org.ln678090.connecthub.notification.entity.TypeNotification;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface NotificationRepository extends JpaRepository<Notification, UUID> {
    long countByRecipientIdAndIsReadFalse(UUID recipientId);

    //Tìm thông báo CÙNG LOẠI, CÙNG BÀI VIẾT, CHƯA ĐỌC để GỘP (Aggregation)
    Optional<Notification> findFirstByRecipientIdAndTypeAndReferenceIdAndIsReadFalse(
            UUID recipientId, TypeNotification type, String referenceId);
//Đánh dấu đã đọc tất cả
    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.recipient.id = :recipientId AND n.isRead = false")
    int markAllAsRead(@Param("recipientId") UUID recipientId);
    // Job xóa thông báo rác cũ hơn X ngày
    @Modifying
    @Query("DELETE FROM Notification n WHERE n.updatedAt < :cutoffDate AND n.isRead = true")
    int deleteOldReadNotifications(@Param("cutoffDate") OffsetDateTime cutoffDate);

//    // 1. Query lấy danh sách thông báo phân trang Cursor (dựa vào updated_at)
//    @Query("SELECT new org.ln678090.connecthub.notification.dto.resp.NotificationResp(" +
//            "n.id, n.type, n.referenceId, n.isRead, n.actorCount, n.updatedAt, " +
//            "n.actor.id, n.actor.fullName, n.actor.avatarUrl) " +
//            "FROM Notification n WHERE n.recipient.id = :recipientId AND n.updatedAt < :cursor " +
//            "ORDER BY n.updatedAt DESC")
//    List<NotificationResp> findByRecipientIdWithCursor(
//            @Param("recipientId") UUID recipientId,
//            @Param("cursor") OffsetDateTime cursor,
//            Pageable pageable);
@Query("SELECT n FROM Notification n " +
        "JOIN FETCH n.actor " + // Tối ưu N+1 Query
        "WHERE n.recipient.id = :recipientId AND n.updatedAt < :cursor " +
        "ORDER BY n.updatedAt DESC")
List<NotificationResp> findByRecipientIdWithCursor(
        @Param("recipientId") UUID recipientId,
        @Param("cursor") OffsetDateTime cursor,
        Pageable pageable);

}