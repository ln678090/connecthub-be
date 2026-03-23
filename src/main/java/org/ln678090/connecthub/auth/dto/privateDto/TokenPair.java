package org.ln678090.connecthub.auth.dto.privateDto;

public record TokenPair(
        String accessToken,
        String refreshToken
) {}