package org.ln678090.connecthub.post.dto.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreatePostRequest(
        @NotBlank(message = "Nội dung không được trống")
        @Size(max = 5000, message = "Nội dung quá dài")
        String content,

        @Size(max = 500, message = "URL ảnh quá dài")
        String imageUrl
) {}