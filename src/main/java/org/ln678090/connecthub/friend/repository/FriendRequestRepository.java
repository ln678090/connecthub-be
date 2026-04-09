package org.ln678090.connecthub.friend.repository;

import org.ln678090.connecthub.friend.entity.FriendRequest;
import org.ln678090.connecthub.friend.entity.FriendRequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

public interface FriendRequestRepository extends JpaRepository<FriendRequest, UUID> {
    boolean existsBySender_IdAndReceiver_IdAndStatus(UUID senderId, UUID receiverId, FriendRequestStatus status);

    boolean existsBySender_IdAndReceiver_Id(UUID senderId, UUID receiverId);

    boolean existsBySender_IdAndReceiver_IdOrSender_IdAndReceiver_Id(
            UUID senderA, UUID receiverA, UUID senderB, UUID receiverB
    );

    boolean existsBySenderIdAndReceiverIdAndStatus(UUID currentUserId, UUID targetUserId, FriendRequestStatus friendRequestStatus);
    Optional<FriendRequest> findBySender_IdAndReceiver_Id(UUID senderId, UUID receiverId);
    @Modifying
    @Transactional
    @Query("DELETE FROM FriendRequest f WHERE f.sender.id = :senderId AND f.receiver.id = :receiverId")
    void deleteBySenderAndReceiver(UUID senderId, UUID receiverId);
    @Modifying
    @Transactional
    @Query("DELETE FROM FriendRequest f WHERE (f.sender.id = :user1 AND f.receiver.id = :user2) OR (f.sender.id = :user2 AND f.receiver.id = :user1)")
    void deleteConnection(UUID user1, UUID user2);
    @Query("SELECT fr FROM FriendRequest fr WHERE fr.status = 'PENDING' AND " +
            "((fr.sender.id = :user1 AND fr.receiver.id = :user2) OR " +
            "(fr.sender.id = :user2 AND fr.receiver.id = :user1))")
    Optional<FriendRequest> findPendingRequestBetween(@Param("user1") UUID user1, @Param("user2") UUID user2);
}