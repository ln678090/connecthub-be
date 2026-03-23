package org.ln678090.connecthub.post.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.ln678090.connecthub.auth.dto.resp.ApiResp;
import org.ln678090.connecthub.auth.utils.SecurityUtils;
import org.ln678090.connecthub.post.dto.req.CreatePostRequest;
import org.ln678090.connecthub.post.dto.resp.PostResponse;
import org.ln678090.connecthub.post.service.PostService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.ScrollPosition;
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
                .message("Tạo bài viết thành công")
                .data(postService.createPost(currentUserId, request))
                .timestamp(Instant.now().toString())
                .build();
    }

    @GetMapping
    public ApiResp<Page<PostResponse>> getFeed(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size
    ) {
        UUID currentUserId = SecurityUtils.currentUserId(authentication);
        return ApiResp.<Page<PostResponse>>builder()
                .message("Lấy danh sách bài viết thành công")
                .data(postService.getFeed(currentUserId, page, size))
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
                .data(Map.of("success", true))
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