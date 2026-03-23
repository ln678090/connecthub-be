package org.ln678090.connecthub.friend.dto.resp;

import java.util.UUID;

public record UserSuggestionResponse(
        UUID id,
        String name,
        String avatar,
        long mutualFriends
) {}