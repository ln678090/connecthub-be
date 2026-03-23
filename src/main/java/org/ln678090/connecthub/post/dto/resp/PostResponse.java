package org.ln678090.connecthub.post.dto.resp;

import java.time.OffsetDateTime;
import java.util.UUID;

public record PostResponse(
        UUID id,
        UUID authorId,
        String authorName,
        String authorAvatar,
        String content,
        String imageUrl,
        OffsetDateTime createdAt,
        long likeCount,
        long commentCount,
        boolean likedByMe
) {}