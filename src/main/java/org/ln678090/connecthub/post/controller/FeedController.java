package org.ln678090.connecthub.post.controller;

import lombok.RequiredArgsConstructor;
import org.ln678090.connecthub.common.dto.resp.ApiResp;
import org.ln678090.connecthub.auth.utils.SecurityUtils;
import org.ln678090.connecthub.post.service.PostService;
import org.springframework.data.domain.ScrollPosition;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
@RestController
@RequiredArgsConstructor
public class FeedController {
    private final PostService postService;

    @GetMapping("/api/feed")
    public ApiResp<Map<String, Object>> getFeed(
            Authentication authentication,
            // Nhận tất cả các query params vào 1 map, ngoại trừ size
            @RequestParam Map<String, String> allParams,
            @RequestParam(defaultValue = "5") int size) {

        UUID currentUserId = SecurityUtils.currentUserId(authentication);

        // Lấy riêng ID và CreatedAt từ URL (nếu có)
        String lastCreatedAt = allParams.get("createdAt");
        String lastId = allParams.get("id");

        ScrollPosition scrollPosition;

        // Nếu client gửi đủ keyset (của bài post cuối cùng ở trang trước)
        if (lastCreatedAt != null && lastId != null && !lastCreatedAt.isBlank()) {

            // Ép kiểu (Parse) createdAt về đúng kiểu Instant/OffsetDateTime tuỳ Entity của bạn
            // Ở đây ví dụ Entity Post của bạn dùng OffsetDateTime:
            java.time.OffsetDateTime parsedDate = java.time.OffsetDateTime.parse(lastCreatedAt);
            UUID parsedId = UUID.fromString(lastId);


            Map<String, Object> keys = Map.of(
                    "createdAt", parsedDate,
                    "id", parsedId
            );


            scrollPosition = ScrollPosition.forward(keys);
        } else {
            // Lần load đầu tiên
            scrollPosition = ScrollPosition.keyset();
        }

        Map<String, Object> feedResult = postService.getFeedCursor(currentUserId, scrollPosition, size);

        return ApiResp.<Map<String, Object>>builder()
                .message("Lấy Feed thành công")
                .data(feedResult)
                .timestamp(Instant.now().toString())
                .build();
    }

}
