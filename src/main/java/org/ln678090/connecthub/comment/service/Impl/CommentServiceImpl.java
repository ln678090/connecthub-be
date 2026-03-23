package org.ln678090.connecthub.comment.service.Impl;

import lombok.RequiredArgsConstructor;
import org.ln678090.connecthub.auth.entity.User;
import org.ln678090.connecthub.auth.repository.UserRepository;
import org.ln678090.connecthub.comment.dto.req.CommentRequest;
import org.ln678090.connecthub.comment.dto.resp.CommentResponse;
import org.ln678090.connecthub.comment.entity.Comment;
import org.ln678090.connecthub.comment.repository.CommentRepository;
import org.ln678090.connecthub.comment.service.CommentService;
import org.ln678090.connecthub.post.entity.Post;
import org.ln678090.connecthub.post.repository.PostRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.AccessDeniedException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService
{
    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    @Transactional
    @Override
    public CommentResponse createComment(UUID postId, UUID userId, CommentRequest request) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy bài viết"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy user"));

        Comment parentComment = null;
        if (request.parentId() != null) {
            parentComment = commentRepository.findById(request.parentId())
                    .orElseThrow(() -> new IllegalArgumentException("Bình luận gốc không tồn tại"));

            // Tùy chọn: Chặn trả lời lồng nhau quá sâu (ép tất cả reply thành level 2)
            if (parentComment.getParent() != null) {
                parentComment = parentComment.getParent();
            }
        }

        Comment comment = Comment.builder()
                .post(post)
                .user(user)
                .parent(parentComment)
                .content(request.content())
                .build();

        comment = commentRepository.save(comment);
        return mapToResponse(comment);
    }
    @Override
    public Page<CommentResponse> getTopLevelComments(UUID postId, Pageable pageable) {
        return commentRepository.findByPostIdAndParentIsNullOrderByCreatedAtDesc(postId, pageable)
                .map(this::mapToResponse);
    }
    @Override
    public Page<CommentResponse> getReplies(UUID parentId, Pageable pageable) {
        return commentRepository.findByParentIdOrderByCreatedAtAsc(parentId, pageable)
                .map(this::mapToResponse);
    }
    @Transactional
    @Override
    public void softDeleteComment(UUID commentId, UUID currentUserId) throws AccessDeniedException {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("Bình luận không tồn tại"));

        if (!comment.getUser().getId().equals(currentUserId)) {
            throw new AccessDeniedException("Bạn không có quyền xóa bình luận này");
        }

        // Hibernate @SQLDelete sẽ tự động chuyển thành câu UPDATE is_deleted = true
        commentRepository.delete(comment);
    }
    private CommentResponse mapToResponse(Comment comment) {
        return new CommentResponse(
                comment.getId(),
                comment.getPost().getId(),
                comment.getParent() != null ? comment.getParent().getId() : null,
                comment.getContent(),
                new CommentResponse.AuthorDto(
                        comment.getUser().getId(),
                        comment.getUser().getFullName(),
                        comment.getUser().getAvatarUrl()
                ),
                comment.getCreatedAt(),
                commentRepository.countByParentId(comment.getId())
        );
    }
}
