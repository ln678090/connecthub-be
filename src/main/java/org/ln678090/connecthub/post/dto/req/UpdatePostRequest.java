package org.ln678090.connecthub.post.dto.req;

import jakarta.validation.constraints.NotBlank;

public record UpdatePostRequest(
        @NotBlank String content,
        String imageUrl
) {}