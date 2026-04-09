package org.ln678090.connecthub.chat.dto;

import lombok.Builder;

import java.util.UUID;

@Builder
public record UserFindUserResp(
        UUID id,
        String fullName,
        String username,
        String avatar,
        boolean isOnline
) {
}