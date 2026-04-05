package org.ln678090.connecthub.notification.controller;


import lombok.RequiredArgsConstructor;
import org.ln678090.connecthub.auth.utils.SecurityUtils;
import org.ln678090.connecthub.common.dto.resp.ApiResp;
import org.ln678090.connecthub.notification.service.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public ResponseEntity<?> getNotifications(
            Authentication auth,
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "10") int limit) {
        OffsetDateTime cursorTime = cursor != null ? OffsetDateTime.parse(cursor) : null;
        return ResponseEntity.ok(ApiResp.builder().message("Thành công")
                .data(notificationService.getNotifications(SecurityUtils.currentUserId(auth), cursorTime, limit))
                .build());
    }

    @GetMapping("/unread-count")
    public ResponseEntity<?> getUnreadCount(Authentication auth) {
        return ResponseEntity.ok(ApiResp.builder().message("Thành công")
                .data(notificationService.getUnreadCount(SecurityUtils.currentUserId(auth)))
                .build());
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<?> markAsRead(@PathVariable UUID id, Authentication auth) {
        notificationService.markAsRead(SecurityUtils.currentUserId(auth), id);
        return ResponseEntity.ok(ApiResp.builder().message("Đã đọc").data(Boolean.TRUE).build());
    }

    @PutMapping("/read-all")
    public ResponseEntity<?> markAllAsRead(Authentication auth) {
        notificationService.markAllAsRead(SecurityUtils.currentUserId(auth));
        return ResponseEntity.ok(ApiResp.builder().message("Đã đọc tất cả").data(Boolean.TRUE).build());
    }
}