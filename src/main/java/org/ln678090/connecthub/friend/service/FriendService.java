package org.ln678090.connecthub.friend.service;

import org.ln678090.connecthub.friend.dto.resp.UserSuggestionResponse;

import java.util.List;
import java.util.UUID;

public interface FriendService {
    void sendRequest(UUID currentUserId, UUID receiverId);
    List<UserSuggestionResponse> getSuggestions(UUID currentUserId);
}
