package org.ln678090.connecthub.post.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.ln678090.connecthub.common.dto.resp.ApiResp;
import org.ln678090.connecthub.auth.utils.SecurityUtils;
import org.ln678090.connecthub.post.dto.req.CreatePostRequest;
import org.ln678090.connecthub.post.dto.resp.PostResponse;
import org.ln678090.connecthub.post.service.PostService;
import org.springframework.data.domain.ScrollPosition;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.nio.file.AccessDeniedException;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @PostMapping
    public ApiResp<PostResponse> createPost(
            Authentication authentication,
            @Valid @RequestBody CreatePostRequest request
    ) {
        UUID currentUserId = SecurityUtils.currentUserId(authentication);
        return ApiResp.<PostResponse>builder()
                .message("Create post success")
                .data(postService.createPost(currentUserId, request))
                .build();

    }
    @GetMapping("/{postId}")
    public ResponseEntity<?> getPostById(
            @PathVariable UUID postId,
           Authentication authentication
    ) {
        UUID currentUserId=SecurityUtils.currentUserId(authentication);
        PostResponse postResponse = postService.getPostById(postId, currentUserId);

        return ResponseEntity.ok(
                ApiResp.builder().message("Lấy bài viết thành công").data(postResponse).build()
        );
    }

    @GetMapping("/users/{userId}")
    public ApiResp<Map<String, Object>> getPostsByUserId(
            @PathVariable UUID userId,
            Authentication authentication,
            @RequestParam Map<String, String> allParams,
            @RequestParam(defaultValue = "5") int size) {

        UUID currentUserId = authentication != null ? SecurityUtils.currentUserId(authentication) : null;

        String lastCreatedAt = allParams.get("createdAt");
        String lastId = allParams.get("id");

        ScrollPosition scrollPosition;

        if (lastCreatedAt != null && lastId != null && !lastCreatedAt.isBlank()) {
            java.time.OffsetDateTime parsedDate = java.time.OffsetDateTime.parse(lastCreatedAt);
            UUID parsedId = UUID.fromString(lastId);

            Map<String, Object> keys = Map.of(
                    "createdAt", parsedDate,
                    "id", parsedId
            );
            scrollPosition = ScrollPosition.forward(keys);
        } else {
            scrollPosition = ScrollPosition.keyset();
        }

        Map<String, Object> result = postService.getPostsByUserIdCursor(userId, currentUserId, scrollPosition, size);

        return ApiResp.<Map<String, Object>>builder()
                .message("Lấy danh sách bài viết thành công")
                .data(result)
                .timestamp(Instant.now().toString())
                .build();
    }


    @PostMapping("/{id}/like")
    public ApiResp<Map<String, Boolean>> toggleLike(
            Authentication authentication,
            @PathVariable UUID id
    ) {
        UUID currentUserId = SecurityUtils.currentUserId(authentication);
        postService.toggleLike(currentUserId, id);

        return ApiResp.<Map<String, Boolean>>builder()
                .message("Cập nhật trạng thái thích thành công")
                .data(Map.of("success", Boolean.TRUE))
                .timestamp(Instant.now().toString())
                .build();
    }
    @DeleteMapping("/{postId}")
    public ApiResp<Void> deletePost(@PathVariable UUID postId, Authentication authentication) throws AccessDeniedException {
        UUID userId = SecurityUtils.currentUserId(authentication);
        postService.softDeletePost(postId, userId);

        return ApiResp.<Void>builder()
                .message("Xóa bài viết thành công")
                .timestamp(Instant.now().toString())
                .build();
    }

}