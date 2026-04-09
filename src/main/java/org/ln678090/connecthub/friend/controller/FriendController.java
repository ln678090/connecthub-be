package org.ln678090.connecthub.friend.controller;


import lombok.RequiredArgsConstructor;
import org.ln678090.connecthub.common.dto.resp.ApiResp;
import org.ln678090.connecthub.auth.utils.SecurityUtils;
import org.ln678090.connecthub.friend.dto.resp.UserSuggestionResponse;
import org.ln678090.connecthub.friend.service.FriendService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class FriendController {

    private final FriendService friendService;

    @GetMapping("/api/users/suggestions")
    public ApiResp<List<UserSuggestionResponse>> getSuggestions(Authentication authentication) {
        UUID currentUserId = SecurityUtils.currentUserId(authentication);
        return ApiResp.<List<UserSuggestionResponse>>builder()
                .message("Lấy gợi ý kết bạn thành công")
                .data(friendService.getSuggestions(currentUserId))
                .build();
    }

    @PostMapping("/api/friends/request/{userId}")
    public ApiResp<Map<String, Boolean>> sendRequest(
            Authentication authentication,
            @PathVariable UUID userId
    ) {
        UUID currentUserId = SecurityUtils.currentUserId(authentication);
        friendService.sendRequest(currentUserId, userId);

        return ApiResp.<Map<String, Boolean>>builder()
                .message("Gửi lời mời kết bạn thành công")
                .data(Map.of("success", true))
                .build();
    }
    @PostMapping("/api/friends/request/{userId}/accept")
    public ApiResp<?> acceptRequest(Authentication authentication, @PathVariable UUID userId) {
        friendService.acceptRequest(SecurityUtils.currentUserId(authentication), userId);
        return ApiResp.builder().message("Đã chấp nhận kết bạn").data(Map.of("success", true)).build();
    }

    @PostMapping("/api/friends/request/{userId}/reject")
    public ApiResp<?> rejectRequest(Authentication authentication, @PathVariable UUID userId) {
        friendService.rejectRequest(SecurityUtils.currentUserId(authentication), userId);
        return ApiResp.builder().message("Đã từ chối kết bạn").data(Map.of("success", true)).build();
    }

    @DeleteMapping("/api/friends/request/{userId}/cancel")
    public ApiResp<?> cancelRequest(Authentication authentication, @PathVariable UUID userId) {
        friendService.cancelRequest(SecurityUtils.currentUserId(authentication), userId);
        return ApiResp.builder().message("Đã hủy lời mời").data(Map.of("success", true)).build();
    }

    @DeleteMapping("/api/friends/{userId}/unfriend")
    public ApiResp<?> unfriend(Authentication authentication, @PathVariable UUID userId) {
        friendService.unfriend(SecurityUtils.currentUserId(authentication), userId);
        return ApiResp.builder().message("Đã hủy kết bạn").data(Map.of("success", true)).build();
    }
    @GetMapping("/api/friends")
    public ResponseEntity<?> getFriends(
            Authentication authentication,
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "10") int limit) {

        UUID currentUserId = SecurityUtils.currentUserId(authentication);
        OffsetDateTime cursorTime = cursor != null ? OffsetDateTime.parse(cursor) : null;

        Map<String, Object> response = friendService.getFriendsList(currentUserId, cursorTime, limit);
        return ResponseEntity.ok(ApiResp.builder().message("Thành công").data(response).build());
    }
    @GetMapping("/api/friends/status/{userA}/{userB}")
    public ResponseEntity<Map<String, String>> getFriendshipStatus(@PathVariable UUID userA, @PathVariable UUID userB) {
        String status = friendService.getUiStatus(userA, userB);


        Map<String, String> response = new HashMap<>();
        response.put("status", status);

        return ResponseEntity.ok(response);
    }
}