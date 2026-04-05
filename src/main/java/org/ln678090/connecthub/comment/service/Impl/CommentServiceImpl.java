package org.ln678090.connecthub.comment.service.Impl;

import lombok.RequiredArgsConstructor;
import org.ln678090.connecthub.auth.entity.User;
import org.ln678090.connecthub.auth.repository.UserRepository;
import org.ln678090.connecthub.comment.dto.req.CommentRequest;
import org.ln678090.connecthub.comment.dto.resp.CommentResponse;
import org.ln678090.connecthub.comment.entity.Comment;
import org.ln678090.connecthub.comment.repository.CommentRepository;
import org.ln678090.connecthub.comment.service.CommentService;
import org.ln678090.connecthub.notification.entity.TypeNotification;
import org.ln678090.connecthub.notification.service.NotificationService;
import org.ln678090.connecthub.post.entity.Post;
import org.ln678090.connecthub.post.repository.PostRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.AccessDeniedException;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService
{
    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

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

        String compositeReferenceId = post.getId().toString() + "_" + comment.getId().toString();

        // GỬI THÔNG BÁO
        if (parentComment != null) {
            // Thông báo có người phản hồi bình luận của mình
            if (!parentComment.getUser().getId().equals(userId)) {
                notificationService.sendNotification(
                        parentComment.getUser().getId(), // Người nhận là chủ cmt gốc
                        userId, // Người cmt
                        TypeNotification.COMMENT_COMMENT_POST,
                        compositeReferenceId
                );
            }
        } else {
            // Thông báo có người bình luận vào bài viết của mình
            if (!post.getUser().getId().equals(userId)) {
                notificationService.sendNotification(
                        post.getUser().getId(), // Chủ bài viết
                        userId,
                        TypeNotification.COMMENT_POST,
                        compositeReferenceId
                );
            }
        }

        return mapToResponse(comment);
    }
    @Override
    public Map<String, Object> getTopLevelComments(UUID postId, OffsetDateTime cursor, int limit) {
        if (cursor == null) cursor = OffsetDateTime.now();

        List<Comment> comments = commentRepository.findByPostIdAndParentIsNullAndCreatedAtLessThanOrderByCreatedAtDesc(
                postId, cursor, PageRequest.of(0, limit + 1)
        );

        boolean hasNext = comments.size() > limit;
        if (hasNext) comments.remove(limit);

        List<CommentResponse> dtos = comments.stream().map(this::mapToResponse).toList();
        String nextCursor = comments.isEmpty() ? null : comments.get(comments.size() - 1).getCreatedAt().toString();

        return Map.of(
                "data", dtos,
                "nextCursor", nextCursor != null ? nextCursor : "",
                "hasNext", hasNext
        );
    }

   @Override
   public Map<String, Object> getReplies(UUID parentId, OffsetDateTime cursor, int limit) {
        // Reply thường cuộn từ trên xuống (cũ -> mới), nên lấy mốc thời gian cũ nhất
        if (cursor == null) cursor = OffsetDateTime.parse("1970-01-01T00:00:00Z");

        List<Comment> comments = commentRepository.findByParentIdAndCreatedAtGreaterThanOrderByCreatedAtAsc(
                parentId, cursor, PageRequest.of(0, limit + 1)
        );

        boolean hasNext = comments.size() > limit;
        if (hasNext) comments.remove(limit);

        List<CommentResponse> dtos = comments.stream().map(this::mapToResponse).toList();
        String nextCursor = comments.isEmpty() ? null : comments.get(comments.size() - 1).getCreatedAt().toString();

       return Map.of(
               "data", dtos,
               "nextCursor", nextCursor != null ? nextCursor : "",
               "hasNext", hasNext
       );
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
    @Override
    public CommentResponse getCommentById(UUID commentId) {
        Comment comment=commentRepository.findById(commentId)   .orElseThrow(() -> new IllegalArgumentException("Bình luận không tồn tại"));
        return mapToResponse(comment);
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
