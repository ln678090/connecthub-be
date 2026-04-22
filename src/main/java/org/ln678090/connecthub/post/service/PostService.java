package org.ln678090.connecthub.post.service;

import org.ln678090.connecthub.post.dto.req.CreatePostRequest;
import org.ln678090.connecthub.post.dto.req.UpdatePostRequest;
import org.ln678090.connecthub.post.dto.resp.PostResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.ScrollPosition;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.AccessDeniedException;
import java.util.Map;
import java.util.UUID;

public interface PostService {
    PostResponse createPost(UUID currentUserId, CreatePostRequest request);
    Page<PostResponse> getFeed(UUID currentUserId, int page, int size);
    void toggleLike(UUID currentUserId, UUID postId);

    @Transactional
    void softDeletePost(UUID postId, UUID currentUserId) throws AccessDeniedException;
    // Thay đổi kiểu dữ liệu tham số truyền vào
    Map<String, Object> getFeedCursor(UUID currentUserId, ScrollPosition scrollPosition, int size);
    Map<String, Object> getPostsByUserIdCursor(UUID targetUserId, UUID currentUserId, ScrollPosition scrollPosition, int size);
    PostResponse getPostById(UUID postId, UUID currentUserId);
    PostResponse updatePost(UUID postId, UUID currentUserId, UpdatePostRequest request) throws AccessDeniedException;
}
