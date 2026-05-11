package org.ln678090.connecthub.friend.repository;

import org.ln678090.connecthub.friend.entity.Follow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.UUID;

public interface FollowRepository extends JpaRepository<Follow, UUID> {
    boolean existsByFollowerIdAndFollowingId(UUID followerId, UUID followingId);

    @Modifying
    @Transactional
    @Query("DELETE FROM Follow f WHERE f.follower.id = :followerId AND f.following.id = :followingId")
    void deleteByFollowerIdAndFollowingId(UUID followerId, UUID followingId);

    long countByFollowingId(UUID followingId); // Số người đang theo dõi user này (Followers)
    long countByFollowerId(UUID followerId);   // Số người user này đang theo dõi (Following)
    @Modifying
    @Query(value = """
insert into follows (id, follower_id,following_id,created_at) 
select :id,:currentUserId ,:targetUserId,:createdAt
 where exists (select 1 from users where id =:currentUserId) 
 and exists (select 1 from users where id =:targetUserId)  
 and not exists (
 select 1 from follows 
  WHERE follower_id = :currentUserId
   AND following_id = :targetUserId
 )
""",nativeQuery = true)
    int insertFollowIfNotExists(
            @Param("id") UUID id,
            @Param("currentUserId") UUID currentUserId,
            @Param("targetUserId") UUID targetUserId,
            @Param("createdAt") OffsetDateTime createdAt
    );
}