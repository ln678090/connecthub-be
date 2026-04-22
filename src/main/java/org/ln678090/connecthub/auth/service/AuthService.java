package org.ln678090.connecthub.auth.service;

import org.ln678090.connecthub.auth.dto.privateDto.TokenPair;
import org.ln678090.connecthub.auth.dto.req.LoginRequest;
import org.ln678090.connecthub.auth.dto.req.RegisterRequest;
import org.springframework.transaction.annotation.Transactional;

public interface AuthService {
    TokenPair login(LoginRequest request);

    @Transactional
    TokenPair register(RegisterRequest request);

    @Transactional
    TokenPair refreshToken(String oldRefreshToken);
    // Trong file AuthService.java
    TokenPair loginWithGoogle(String idTokenString);
    void logout(String refreshToken);
}
