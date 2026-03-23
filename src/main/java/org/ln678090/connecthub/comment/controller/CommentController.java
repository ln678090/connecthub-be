package org.ln678090.connecthub.comment.controller;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.ln678090.connecthub.auth.dto.resp.ApiResp;
import org.ln678090.connecthub.auth.utils.SecurityUtils;
import org.ln678090.connecthub.comment.dto.req.CommentRequest;
import org.ln678090.connecthub.comment.dto.resp.CommentResponse;
import org.ln678090.connecthub.comment.service.Impl.CommentServiceImpl;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.nio.file.AccessDeniedException;
import java.time.Instant;
import java.util.UUID;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class CommentController {
    private final CommentServiceImpl commentService;

    @PostMapping("/{postId}/comments")
    public ApiResp<CommentResponse> addComment(
            @PathVariable UUID postId,
            @Valid @RequestBody CommentRequest request,
            Authentication authentication) {
        UUID userId = SecurityUtils.currentUserId(authentication);
        CommentResponse response = commentService.createComment(postId, userId, request);
        return ApiResp.<CommentResponse>builder()
                .message("Bình luận thành công")
                .data(response)
                .timestamp(Instant.now().toString())
                .build();
    }

    @GetMapping("/{postId}/comments")
    public ApiResp<Page<CommentResponse>> getComments(
            @PathVariable UUID postId, Pageable pageable) {
        return ApiResp.<Page<CommentResponse>>builder()
                .message("Lấy danh sách bình luận thành công")
                .data(commentService.getTopLevelComments(postId, pageable))
                .timestamp(Instant.now().toString())
                .build();
    }

    @GetMapping("/comments/{parentId}/replies")
    public ApiResp<Page<CommentResponse>> getReplies(
            @PathVariable UUID parentId, Pageable pageable) {
        return ApiResp.<Page<CommentResponse>>builder()
                .message("Lấy danh sách trả lời thành công")
                .data(commentService.getReplies(parentId, pageable))
                .timestamp(Instant.now().toString())
                .build();
    }

    @DeleteMapping("/comments/{commentId}")
    public ApiResp<Void> deleteComment(
            @PathVariable UUID commentId, Authentication authentication) throws AccessDeniedException {
        UUID userId = SecurityUtils.currentUserId(authentication);
        commentService.softDeleteComment(commentId, userId);
        return ApiResp.<Void>builder()
                .message("Xóa bình luận thành công")
                .timestamp(Instant.now().toString())
                .build();
    }
}