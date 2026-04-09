package org.ln678090.connecthub.friend.service;

import org.ln678090.connecthub.friend.dto.resp.UserSuggestionResponse;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface FriendService {
    void sendRequest(UUID currentUserId, UUID receiverId);
    List<UserSuggestionResponse> getSuggestions(UUID currentUserId);
    void acceptRequest(UUID currentUserId, UUID senderId);
    void rejectRequest(UUID currentUserId, UUID senderId);
    void cancelRequest(UUID currentUserId, UUID receiverId);
    void unfriend(UUID currentUserId, UUID friendId);
    Map<String, Object> getFriendsList(UUID userId, OffsetDateTime cursor, int limit);
    void followUser(UUID currentUserId, UUID targetUserId);
    void unfollowUser(UUID currentUserId, UUID targetUserId);

    String getUiStatus(UUID userA, UUID userB);
}
