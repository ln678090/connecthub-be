package org.ln678090.connecthub.friend.controller;

import lombok.RequiredArgsConstructor;
import org.ln678090.connecthub.auth.utils.SecurityUtils;
import org.ln678090.connecthub.common.dto.resp.ApiResp;
import org.ln678090.connecthub.friend.service.FriendService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/follow")
public class FollowController {
    private final FriendService friendService;

    @PostMapping("/{userId}")
    public ResponseEntity<?> followUser(@PathVariable UUID userId, Authentication auth) {
        UUID currentUserId = SecurityUtils.currentUserId(auth);
        friendService.followUser(currentUserId, userId);

        return ResponseEntity.ok(ApiResp.builder()
                .message("Đã theo dõi")
                .data(true)
                .build());
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<?> unfollowUser(@PathVariable UUID userId, Authentication auth) {
        UUID currentUserId = SecurityUtils.currentUserId(auth);
        friendService.unfollowUser(currentUserId, userId);

        return ResponseEntity.ok(ApiResp.builder()
                .message("Đã bỏ theo dõi")
                .data(true)
                .build());
    }
}
