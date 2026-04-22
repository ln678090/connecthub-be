package org.ln678090.connecthub.comment.controller;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.ln678090.connecthub.common.dto.resp.ApiResp;
import org.ln678090.connecthub.auth.utils.SecurityUtils;
import org.ln678090.connecthub.comment.dto.req.CommentRequest;
import org.ln678090.connecthub.comment.dto.resp.CommentResponse;
import org.ln678090.connecthub.comment.service.Impl.CommentServiceImpl;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.nio.file.AccessDeniedException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.Map;
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
    @PutMapping("/comments/{commentId}")
    public ApiResp<CommentResponse> updateComment(
            @PathVariable UUID commentId,
            @Valid @RequestBody CommentRequest request,
            Authentication authentication) throws AccessDeniedException {
        UUID userId = SecurityUtils.currentUserId(authentication);
        return ApiResp.<CommentResponse>builder()
                .message("Cập nhật bình luận thành công")
                .data(commentService.updateComment(commentId, userId, request))
                .timestamp(Instant.now().toString())
                .build();
    }
    @GetMapping("/{postId}/comments")
    public ApiResp<Map<String, Object>> getComments(
            @PathVariable UUID postId,
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "10") int limit) {

        // Chuyển đổi chuỗi thời gian từ Frontend gửi lên thành OffsetDateTime
        OffsetDateTime cursorTime = (cursor != null && !cursor.isEmpty())
                ? OffsetDateTime.parse(cursor)
                : null;

        return ApiResp.<Map<String, Object>>builder()
                .message("Lấy danh sách bình luận thành công")
                .data(commentService.getTopLevelComments(postId, cursorTime, limit))
                .timestamp(Instant.now().toString())
                .build();
    }

    @GetMapping("/comments/{parentId}/replies")
    public ApiResp<Map<String, Object>> getReplies(
            @PathVariable UUID parentId,
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "5") int limit) { // Trả lời thì thường lấy ít hơn (vd: 5)

        OffsetDateTime cursorTime = (cursor != null && !cursor.isEmpty())
                ? OffsetDateTime.parse(cursor)
                : null;

        return ApiResp.<Map<String, Object>>builder()
                .message("Lấy danh sách trả lời thành công")
                .data(commentService.getReplies(parentId, cursorTime, limit))
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
    @GetMapping("/comments/{commentId}/detail")
    public ApiResp<Object> getCommentById(@PathVariable UUID commentId) {
        CommentResponse comment = commentService.getCommentById(commentId);
        return ApiResp.builder()
                .message("lấy bình luận thành công")
                .data(comment)
                .build();
    }
}