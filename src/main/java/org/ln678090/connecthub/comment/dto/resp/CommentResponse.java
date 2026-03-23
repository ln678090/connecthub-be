package org.ln678090.connecthub.comment.dto.resp;

import java.time.OffsetDateTime;
import java.util.UUID;

public record CommentResponse(
        UUID id,
        UUID postId,
        UUID parentId,
        String content,
        AuthorDto author,
        OffsetDateTime createdAt,
        int replyCount // Số lượng câu trả lời con
) {
    public record AuthorDto(UUID id, String fullName, String avatarUrl) {}
}