package org.ln678090.connecthub.friend.repository;

import lombok.NonNull;
import org.ln678090.connecthub.friend.entity.Friendship;
import org.ln678090.connecthub.friend.entity.FriendshipId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FriendshipRepository extends JpaRepository<Friendship, FriendshipId> {
    boolean existsById(@NonNull FriendshipId id);
}