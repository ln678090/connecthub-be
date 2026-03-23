package org.ln678090.connecthub.friend.controller;


import lombok.RequiredArgsConstructor;
import org.ln678090.connecthub.auth.dto.resp.ApiResp;
import org.ln678090.connecthub.auth.utils.SecurityUtils;
import org.ln678090.connecthub.friend.dto.resp.UserSuggestionResponse;
import org.ln678090.connecthub.friend.service.FriendService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
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
                .timestamp(Instant.now().toString())
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
                .timestamp(Instant.now().toString())
                .build();
    }
}