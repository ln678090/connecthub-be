package org.ln678090.connecthub.comment.dto.req;

import jakarta.validation.constraints.NotBlank;

import java.util.UUID;

public record CommentRequest(
        @NotBlank(message = "Nội dung không được để trống")
        String content,
        UUID parentId // Null nếu là bình luận gốc
) {}
