package org.ln678090.connecthub.friend.service.Impl;


import lombok.RequiredArgsConstructor;
import org.ln678090.connecthub.auth.entity.User;
import org.ln678090.connecthub.auth.repository.UserRepository;
import org.ln678090.connecthub.friend.dto.resp.UserSuggestionResponse;
import org.ln678090.connecthub.friend.entity.FriendRequest;
import org.ln678090.connecthub.friend.entity.FriendRequestStatus;
import org.ln678090.connecthub.friend.entity.FriendshipId;
import org.ln678090.connecthub.friend.repository.FriendRequestRepository;
import org.ln678090.connecthub.friend.repository.FriendshipRepository;
import org.ln678090.connecthub.friend.service.FriendService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FriendServiceImpl implements FriendService {

    private final UserRepository userRepository;
    private final FriendRequestRepository friendRequestRepository;
    private final FriendshipRepository friendshipRepository;

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
}