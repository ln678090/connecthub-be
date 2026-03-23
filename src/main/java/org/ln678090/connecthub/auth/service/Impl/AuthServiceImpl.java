package org.ln678090.connecthub.auth.service.Impl;

import lombok.RequiredArgsConstructor;
import org.ln678090.connecthub.auth.config.custom.CustomUserDetails;
import org.ln678090.connecthub.auth.config.jwt.TokenService;
import org.ln678090.connecthub.auth.dto.privateDto.TokenPair;
import org.ln678090.connecthub.auth.dto.req.LoginRequest;
import org.ln678090.connecthub.auth.dto.req.RegisterRequest;
import org.ln678090.connecthub.auth.entity.Role;
import org.ln678090.connecthub.auth.entity.User;
import org.ln678090.connecthub.auth.repository.RoleRepository;
import org.ln678090.connecthub.auth.repository.UserRepository;
import org.ln678090.connecthub.auth.service.AuthService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final TokenService tokenService;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public TokenPair login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        String roles = userDetails.getRolesAsString();
        String accessToken = tokenService.generateAccessToken(userDetails.id(), roles);
        String refreshToken = tokenService.generateRefreshToken(userDetails.id(), roles);
        return new TokenPair(accessToken, refreshToken);
    }

    @Transactional
    @Override
    public TokenPair register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("Email already exists ");
        }
        Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new RuntimeException("Role mặc định không tồn tại "));
        User newUser = new User(
                null,
                request.email(),
                passwordEncoder.encode(request.password()),
                request.fullName(),
                null,
                true,
                OffsetDateTime.now(),
                OffsetDateTime.now(),
                Set.of(userRole)
        );

        userRepository.save(newUser);
        CustomUserDetails userDetails = CustomUserDetails.fromUser(newUser);
        return new TokenPair(
                tokenService.generateAccessToken(userDetails.id(), userDetails.getRolesAsString()),
                tokenService.generateRefreshToken(userDetails.id(), userDetails.getRolesAsString())
        );
    }

    @Override

    public TokenPair refreshToken(String oldRefreshToken) {
        String redisValue = tokenService.getRedisValueFromRefreshToken(oldRefreshToken);
        if (redisValue == null) {
            throw new BadCredentialsException("Refresh Token không hợp lệ hoặc đã hết hạn");
        }
        // Tách ID và Roles từ chuỗi "ID|ROLES"
        String[] parts = redisValue.split("\\|");
        UUID userId = UUID.fromString(parts[0]);
        String roles = parts.length > 1 ? parts[1] : "";
        String newAccessToken = tokenService.generateAccessToken(userId, roles);
        return new TokenPair(newAccessToken, oldRefreshToken);
    }

    @Override
    public void logout(String refreshToken) {
        if (refreshToken == null || refreshToken.isEmpty())  return;
        tokenService.deleteRefreshToken(refreshToken);
    }


}
