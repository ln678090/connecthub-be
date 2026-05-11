package org.ln678090.connecthub.post.repository;

import org.ln678090.connecthub.post.entity.PostLike;
import org.ln678090.connecthub.post.entity.PostLikeId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface PostLikeRepository extends JpaRepository<PostLike, PostLikeId> {
    boolean existsById(PostLikeId id);

    long countByPost_Id(UUID postId);

    void deleteById(PostLikeId id);

    Set<PostLike> findByUser_IdAndPost_IdIn(UUID userId, Collection<UUID> postIds);

    @Query("SELECT pl.post.id, COUNT(pl) FROM PostLike pl WHERE pl.post.id IN :postIds GROUP BY pl.post.id")
    List<Object[]> countLikesByPostIds(@Param("postIds") Set<UUID> postIds);

}