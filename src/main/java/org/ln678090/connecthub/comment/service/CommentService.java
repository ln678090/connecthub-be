package org.ln678090.connecthub.comment.service;

import org.ln678090.connecthub.comment.dto.req.CommentRequest;
import org.ln678090.connecthub.comment.dto.resp.CommentResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.AccessDeniedException;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

public interface CommentService {
    @Transactional
    CommentResponse createComment(UUID postId, UUID userId, CommentRequest request);



    Map<String, Object> getTopLevelComments(UUID postId, OffsetDateTime cursor, int limit);

    Map<String, Object> getReplies(UUID parentId, OffsetDateTime cursor, int limit);

    @Transactional
    void softDeleteComment(UUID commentId, UUID currentUserId) throws AccessDeniedException;

    CommentResponse getCommentById(UUID commentId);
    @Transactional
    CommentResponse updateComment(UUID commentId, UUID currentUserId, CommentRequest request) throws AccessDeniedException;
}
