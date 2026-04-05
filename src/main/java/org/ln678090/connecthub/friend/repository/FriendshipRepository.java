package org.ln678090.connecthub.friend.repository;

import lombok.NonNull;
import org.ln678090.connecthub.friend.dto.resp.FriendItemResp;
import org.ln678090.connecthub.friend.entity.Friendship;
import org.ln678090.connecthub.friend.entity.FriendshipId;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public interface FriendshipRepository extends JpaRepository<Friendship, FriendshipId> {
    boolean existsById(@NonNull FriendshipId id);
    @Modifying
    @Transactional
    @Query("DELETE FROM Friendship f WHERE (f.user.id = :user1 AND f.friend.id = :user2) OR (f.user.id  = :user2 AND f.friend.id = :user1)")
    void deleteFriendship(UUID user1, UUID user2);
    @Query("SELECT new org.ln678090.connecthub.friend.dto.resp.FriendItemResp(f.friend.id, f.friend.fullName, f.friend.avatarUrl, f.friend.bio, f.createdAt) " +
            "FROM Friendship f WHERE f.user.id = :userId AND f.createdAt < :cursor ORDER BY f.createdAt DESC")
    List<FriendItemResp> findFriendsWithCursor(@Param("userId") UUID userId, @Param("cursor") OffsetDateTime cursor, Pageable pageable);

}