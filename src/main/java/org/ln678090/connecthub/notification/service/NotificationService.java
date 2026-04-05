package org.ln678090.connecthub.notification.service;

import org.ln678090.connecthub.notification.entity.TypeNotification;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

public interface NotificationService {
    @Transactional
    void sendNotification(UUID recipientId, UUID actorId, TypeNotification type, String referenceId);

    Map<String, Object> getNotifications(UUID userId, OffsetDateTime cursor, int limit);

    long getUnreadCount(UUID userId);

    @Transactional
    void markAsRead(UUID userId, UUID notifId);

    @Transactional
    void markAllAsRead(UUID userId);
}
