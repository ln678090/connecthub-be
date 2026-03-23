package org.ln678090.connecthub.comment.service;

import org.ln678090.connecthub.comment.dto.req.CommentRequest;
import org.ln678090.connecthub.comment.dto.resp.CommentResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.AccessDeniedException;
import java.util.UUID;

public interface CommentService {
    @Transactional
    CommentResponse createComment(UUID postId, UUID userId, CommentRequest request);

    Page<CommentResponse> getTopLevelComments(UUID postId, Pageable pageable);

    Page<CommentResponse> getReplies(UUID parentId, Pageable pageable);

    @Transactional
    void softDeleteComment(UUID commentId, UUID currentUserId) throws AccessDeniedException;
}
