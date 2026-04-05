package org.ln678090.connecthub.friend.dto.resp;

import java.time.OffsetDateTime;
import java.util.UUID;

public record FriendItemResp(
        UUID id,
        String fullName,
        String avatarUrl,
        String bio,
        OffsetDateTime connectedAt
) {}