package org.ln678090.connecthub.friend.service.Impl;


import lombok.RequiredArgsConstructor;
import org.ln678090.connecthub.auth.entity.User;
import org.ln678090.connecthub.auth.repository.UserRepository;
import org.ln678090.connecthub.friend.dto.resp.FriendItemResp;
import org.ln678090.connecthub.friend.dto.resp.UserSuggestionResponse;
import org.ln678090.connecthub.friend.entity.*;
import org.ln678090.connecthub.friend.repository.FollowRepository;
import org.ln678090.connecthub.friend.repository.FriendRequestRepository;
import org.ln678090.connecthub.friend.repository.FriendshipRepository;
import org.ln678090.connecthub.friend.service.FriendService;
import org.ln678090.connecthub.notification.entity.TypeNotification;
import org.ln678090.connecthub.notification.service.NotificationService;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.awt.print.Pageable;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FriendServiceImpl implements FriendService {

    private final UserRepository userRepository;
    private final FriendRequestRepository friendRequestRepository;
    private final FriendshipRepository friendshipRepository;
    private final FollowRepository followRepository;
    private final NotificationService notificationService;


    @Override
    @Transactional
    public void sendRequest(UUID currentUserId, UUID receiverId) {
        if (currentUserId.equals(receiverId)) {
            throw new IllegalArgumentException("Không thể gửi lời mời cho chính mình");
        }

        User sender = userRepository.findById(currentUserId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người gửi"));
        User receiver = userRepository.findById(receiverId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người nhận"));

        boolean alreadyFriends =
                friendshipRepository.existsById(new FriendshipId(currentUserId, receiverId)) ||
                        friendshipRepository.existsById(new FriendshipId(receiverId, currentUserId));

        if (alreadyFriends) {
            throw new IllegalArgumentException("Hai người đã là bạn");
        }

        boolean alreadyRequested = friendRequestRepository
                .existsBySender_IdAndReceiver_IdOrSender_IdAndReceiver_Id(
                        currentUserId, receiverId, receiverId, currentUserId
                );

        if (alreadyRequested) {
            throw new IllegalArgumentException("Lời mời kết bạn đã tồn tại");
        }

        FriendRequest request = new FriendRequest();
        request.setSender(sender);
        request.setReceiver(receiver);
        request.setStatus(FriendRequestStatus.PENDING);
        request.setCreatedAt(OffsetDateTime.now());
        request.setUpdatedAt(OffsetDateTime.now());

        friendRequestRepository.save(request);

        notificationService.sendNotification(
                receiverId, currentUserId, TypeNotification.ADD_FRIEND, currentUserId.toString()
        );
    }


@Override
@Transactional(readOnly = true)
public List<UserSuggestionResponse> getSuggestions(UUID currentUserId) {
    return userRepository.findTop10ByIdNotOrderByCreatedAtDesc(currentUserId)
            .stream()
            .map(user -> new UserSuggestionResponse(
                    user.getId(),
                    user.getFullName(),
                    user.getAvatarUrl(),
                    0L
            ))
            .toList();
}

@Override
@Transactional
public void acceptRequest(UUID currentUserId, UUID senderId) {
    FriendRequest request = friendRequestRepository.findBySender_IdAndReceiver_Id(senderId, currentUserId)
            .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy lời mời"));

    // 1. Cập nhật trạng thái
    request.setStatus(FriendRequestStatus.ACCEPTED);
    friendRequestRepository.save(request);

    // 2. Truy vấn User từ Database
    User currentUser = userRepository.findById(currentUserId)
            .orElseThrow(() -> new IllegalArgumentException("Lỗi: Không tìm thấy current user"));
    User senderUser = userRepository.findById(senderId)
            .orElseThrow(() -> new IllegalArgumentException("Lỗi: Không tìm thấy sender user"));

    // 3. Lưu bạn bè chiều đi (User -> Friend)
    Friendship f1 = new Friendship();
    f1.setId(new FriendshipId(currentUserId, senderId));
    f1.setUser(currentUser);
    f1.setFriend(senderUser);
    friendshipRepository.save(f1);

    // 4. Lưu bạn bè chiều ngược lại (Friend -> User)
    Friendship f2 = new Friendship();
    f2.setId(new FriendshipId(senderId, currentUserId));
    f2.setUser(senderUser);
    f2.setFriend(currentUser);
    friendshipRepository.save(f2);

    notificationService.sendNotification(
            senderId, currentUserId, TypeNotification.ACCEPT_FRIEND, currentUserId.toString()
    );

}

@Override
@Transactional
public void rejectRequest(UUID currentUserId, UUID senderId) {
    // Xóa lời mời (hoặc bạn có thể set status = REJECTED tùy logic)
    friendRequestRepository.deleteBySenderAndReceiver(senderId, currentUserId);
}

@Override
@Transactional
public void cancelRequest(UUID currentUserId, UUID receiverId) {
    // Mình gửi nhưng đổi ý hủy
    friendRequestRepository.deleteBySenderAndReceiver(currentUserId, receiverId);
}

@Override
@Transactional
public void unfriend(UUID currentUserId, UUID friendId) {
    // Xóa trong bảng Friendship (cả 2 chiều)
    friendshipRepository.deleteFriendship(currentUserId, friendId);
    // Xóa cả record trong FriendRequest để reset trạng thái
    friendRequestRepository.deleteConnection(currentUserId, friendId);
}

@Override
public Map<String, Object> getFriendsList(UUID userId, OffsetDateTime cursor, int limit) {
    // Nếu không truyền cursor (trang đầu tiên), lấy thời điểm hiện tại
    if (cursor == null) {
        cursor = OffsetDateTime.now();
    }

    // Lấy limit + 1 để biết xem có còn dữ liệu cho trang tiếp theo hay không
    List<FriendItemResp> friends = friendshipRepository.findFriendsWithCursor(
            userId, cursor, PageRequest.of(0, limit + 1)
    );

    boolean hasNext = friends.size() > limit;
    if (hasNext) {
        friends.remove(limit); // Bỏ phần tử dư thừa (chỉ dùng để check)
    }

    String nextCursor = friends.isEmpty() ? null : friends.get(friends.size() - 1).connectedAt().toString();

    return Map.of(
            "data", friends,
            "nextCursor", nextCursor != null ? nextCursor : "",
            "hasNext", hasNext
    );
}

@Override
@Transactional
public void followUser(UUID currentUserId, UUID targetUserId) {
    // Kiểm tra xem có tự follow chính mình không
    if (currentUserId.equals(targetUserId)) {
        throw new IllegalArgumentException("Không thể tự theo dõi chính mình");
    }


    User targetuser = userRepository.findById(targetUserId).orElseThrow(
            () -> new IllegalArgumentException("Người dùng không tồn tại")
    );
    User currentUser = userRepository.findById(currentUserId).orElseThrow(
            () -> new IllegalArgumentException("Người dùng không tồn tại")
    );


    // Kiểm tra xem đã follow chưa (tránh duplicate)
    if (!followRepository.existsByFollowerIdAndFollowingId(currentUserId, targetUserId)) {
        Follow follow = new Follow();

        follow.setFollower(currentUser);
        follow.setFollowing(targetuser);
        followRepository.save(follow);

        notificationService.sendNotification(
                targetUserId, currentUserId, TypeNotification.FOLLOW, currentUserId.toString()
        );
    }
}

@Override
@Transactional
public void unfollowUser(UUID currentUserId, UUID targetUserId) {
    // Spring Data JPA dùng @Modifying @Query nên cần được bọc trong @Transactional
    followRepository.deleteByFollowerIdAndFollowingId(currentUserId, targetUserId);
}
}