package org.ln678090.connecthub.post.repository;

import org.ln678090.connecthub.post.dto.resp.PostResponse;
import org.ln678090.connecthub.post.entity.Post;
import org.springframework.data.domain.Limit;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.ScrollPosition;
import org.springframework.data.domain.Window;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface PostRepository extends JpaRepository<Post, UUID> {

    @EntityGraph(attributePaths = {"user"})
    Page<Post> findAllByOrderByCreatedAtDesc(Pageable pageable);
    @Query("""
        SELECT new org.ln678090.connecthub.post.dto.resp.PostResponse(
            p.id, u.id, u.fullName, u.avatarUrl, p.content, p.imageUrl, p.createdAt,
            (SELECT COUNT(pl) FROM PostLike pl WHERE pl.post.id = p.id),
            (SELECT COUNT(c) FROM Comment c WHERE c.post.id = p.id),
            (CASE WHEN EXISTS (SELECT 1 FROM PostLike pl2 WHERE pl2.post.id = p.id AND pl2.user.id = :currentUserId) THEN true ELSE false END)
        )
        FROM Post p JOIN p.user u
        ORDER BY p.createdAt DESC
    """)
    Page<PostResponse> getFeedOptimized(@Param("currentUserId") UUID currentUserId, Pageable pageable);


//    @Query("""
//        SELECT new org.ln678090.connecthub.post.dto.resp.PostResponse(
//            p.id, u.id, u.fullName, u.avatarUrl, p.content, p.imageUrl, p.createdAt,
//            0L, 0L, false
//        )
//        FROM Post p JOIN p.user u
//""")
//    Window<PostResponse> findFeedByCursor(ScrollPosition position, Limit limit);

    @EntityGraph(attributePaths = {"user"}) // Lấy kèm User để tránh N+1
    Window<Post> findAllByOrderByCreatedAtDescIdDesc(ScrollPosition position, Limit limit);
}