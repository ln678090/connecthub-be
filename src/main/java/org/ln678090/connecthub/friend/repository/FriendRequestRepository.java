package org.ln678090.connecthub.friend.repository;

import org.ln678090.connecthub.friend.entity.FriendRequest;
import org.ln678090.connecthub.friend.entity.FriendRequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface FriendRequestRepository extends JpaRepository<FriendRequest, UUID> {
    boolean existsBySender_IdAndReceiver_IdAndStatus(UUID senderId, UUID receiverId, FriendRequestStatus status);

    boolean existsBySender_IdAndReceiver_Id(UUID senderId, UUID receiverId);

    boolean existsBySender_IdAndReceiver_IdOrSender_IdAndReceiver_Id(
            UUID senderA, UUID receiverA, UUID senderB, UUID receiverB
    );
}