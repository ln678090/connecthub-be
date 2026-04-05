package org.ln678090.connecthub.notification.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ln678090.connecthub.auth.repository.UserRepository;
import org.ln678090.connecthub.notification.dto.resp.NotificationResp;
import org.ln678090.connecthub.notification.entity.Notification;
import org.ln678090.connecthub.notification.entity.TypeNotification;
import org.ln678090.connecthub.notification.repository.NotificationRepository;
import org.ln678090.connecthub.notification.service.NotificationService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final StringRedisTemplate redisTemplate;

    @Transactional
    @Override
    public void sendNotification(UUID recipientId, UUID actorId, TypeNotification  type, String referenceId) {
        // 1. Không gửi thông báo cho chính mình (VD: Tự like bài mình)
        if (recipientId.equals(actorId)) return;

        String postId = referenceId.contains("_") ? referenceId.split("_")[0] : referenceId;
        // 2. Chống Spam bằng Redis (Debounce 60 giây)
        // Ví dụ: Bấm Like -> Bỏ Like -> Bấm Like trong 1 phút thì chỉ tính 1 lần
        String redisKey = String.format("notif:spam:%s:%s:%s:%s", recipientId, actorId, type, referenceId);
        Boolean isSpam = redisTemplate.opsForValue().setIfAbsent(redisKey, "1", 60, TimeUnit.SECONDS);
        if (Boolean.FALSE.equals(isSpam)) {
            log.info("Bỏ qua thông báo do chống spam rate-limit: {}", redisKey);
            return;
        }

        // 3. Logic Gộp thông báo (Aggregation)
        // Nếu đã có thông báo loại này (ví dụ LIKE bài A) chưa đọc -> Tăng biến đếm, cập nhật người like cuối
        notificationRepository.findFirstByRecipientIdAndTypeAndReferenceIdAndIsReadFalse(recipientId,   type, postId)
                .ifPresentOrElse(
                        existingNotif -> {
                            // Nếu người vừa thao tác khác với người cũ thì mới tăng count
                            if (!existingNotif.getActor().getId().equals(actorId)) {
                                existingNotif.setActorCount(existingNotif.getActorCount() + 1);
                            }
                            existingNotif.setActor(userRepository.getReferenceById(actorId));
                            existingNotif.setUpdatedAt(OffsetDateTime.now()); // Kéo thông báo lên đầu
                            notificationRepository.save(existingNotif);
                        },
                        () -> {
                            // Nếu chưa có, tạo thông báo mới hoàn toàn
                            Notification newNotif = Notification.builder()
                                    .recipient(userRepository.getReferenceById(recipientId))
                                    .actor(userRepository.getReferenceById(actorId))
                                    .type(type)
                                    .referenceId(referenceId)
                                    .actorCount(1)
                                    .isRead(Boolean.FALSE)
                                    .build();
                            notificationRepository.save(newNotif);
                        }
                );
    }
    @Override
    public Map<String, Object> getNotifications(UUID userId, OffsetDateTime cursor, int limit) {
        if (cursor == null) cursor = OffsetDateTime.now();

        List<NotificationResp> notifs = notificationRepository.findByRecipientIdWithCursor(
                userId, cursor, PageRequest.of(0, limit + 1)
        );

        boolean hasNext = notifs.size() > limit;
        if (hasNext) notifs.remove(limit);

        String nextCursor = notifs.isEmpty() ? null : notifs.get(notifs.size() - 1).getUpdatedAt().toString();

        return Map.of(
                "data", notifs,
                "nextCursor", nextCursor!=null?nextCursor:"",
                "hasNext", hasNext
        );
    }
    @Override
    public long getUnreadCount(UUID userId) {
        return notificationRepository.countByRecipientIdAndIsReadFalse(userId);
    }
    @Transactional
    @Override
    public void markAsRead(UUID userId, UUID notifId) {
        notificationRepository.findById(notifId).ifPresent(n -> {
            if (n.getRecipient().getId().equals(userId)) {
                n.setIsRead(Boolean.TRUE);
                notificationRepository.save(n);
            }
        });
    }
    @Transactional
    @Override
    public void markAllAsRead(UUID userId) {
        notificationRepository.markAllAsRead(userId);
    }
    @Scheduled(cron = "0 40 23 * * *")
    @Transactional
    public void cleanupOldNotifications() {
        OffsetDateTime thirtyDaysAgo = OffsetDateTime.now().minusDays(30);
        int deleted = notificationRepository.deleteOldReadNotifications(thirtyDaysAgo);
        log.info("Đã dọn dẹp {} thông báo cũ đã đọc", deleted);
    }
}
