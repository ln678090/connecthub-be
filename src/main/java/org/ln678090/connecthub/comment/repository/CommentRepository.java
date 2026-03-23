package org.ln678090.connecthub.comment.repository;

import org.ln678090.connecthub.comment.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CommentRepository extends JpaRepository<Comment, UUID> {
    Page<Comment> findByPostIdAndParentIsNullOrderByCreatedAtDesc(UUID postId, Pageable pageable);

    // Lấy danh sách các câu trả lời của 1 bình luận
    Page<Comment> findByParentIdOrderByCreatedAtAsc(UUID parentId, Pageable pageable);

    // Đếm số lượng trả lời con
    int countByParentId(UUID parentId);
}