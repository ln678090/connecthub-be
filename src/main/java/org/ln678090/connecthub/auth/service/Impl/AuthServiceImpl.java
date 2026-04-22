package org.ln678090.connecthub.auth.service.Impl;

import com.github.f4b6a3.uuid.UuidCreator;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
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
import org.ln678090.connecthub.chat.dto.SyncUserDto;
import org.ln678090.connecthub.chat.service.ChatRealtimeService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.Optional;
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
    private final ChatRealtimeService chatRealtimeService;
    @Value("${app.google.client-id}")
    private String googleClientId;

    // ... (Giữ nguyên hàm login, register hiện tại)

    @Transactional
    @Override
    public TokenPair loginWithGoogle(String idTokenString) {
        try {
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), new GsonFactory())
                    .setAudience(Collections.singletonList(googleClientId))
                    .build();

            GoogleIdToken idToken = verifier.verify(idTokenString);
            if (idToken == null) {
                throw new RuntimeException("Token Google không hợp lệ hoặc đã hết hạn.");
            }

            GoogleIdToken.Payload payload = idToken.getPayload();
            String email = payload.getEmail();
            String name = (String) payload.get("name");
            String pictureUrl = (String) payload.get("picture");

            Optional<User> userOptional = userRepository.findByEmail(email); // Dùng hàm findByEmail có sẵn
            User user;

            if (userOptional.isPresent()) {
                user = userOptional.get();

                // KIỂM TRA TÀI KHOẢN ĐÃ BỊ KHÓA HAY CHƯA
                if (user.getIsEnabled() != null && !user.getIsEnabled()) {
                    throw new DisabledException("Tài khoản của bạn đã bị vô hiệu hóa.");
                }
            } else {
                Role userRole = roleRepository.findByName("ROLE_USER")
                        .orElseThrow(() -> new RuntimeException("Role mặc định không tồn tại"));

                // Sinh ra username tự động nếu entity yêu cầu
                String prefix = email.contains("@") ? email.split("@")[0] : UUID.randomUUID().toString().substring(0, 8);

                // Mật khẩu ngẫu nhiên cho user đăng nhập Google
                String randomPassword = passwordEncoder.encode(UUID.randomUUID().toString());

                user = User.builder()
                        .id(UuidCreator.getTimeOrderedEpoch())
                        .email(email)
                        .fullName(name)
                        .passwordHash(randomPassword)
                        .avatarUrl(pictureUrl)
                        .username(prefix) // Thêm username tự động
                        .isEnabled(Boolean.TRUE)
                        .createdAt(OffsetDateTime.now())
                        .updatedAt(OffsetDateTime.now())
                        .roles(Set.of(userRole))
                        .build();

                user = userRepository.save(user);
            }

            // Map qua CustomUserDetails để sinh Token
            CustomUserDetails userDetails = CustomUserDetails.fromUser(user);

            // Trả về Access Token và Refresh Token
            String accessToken = tokenService.generateAccessToken(userDetails.id(), userDetails.getRolesAsString());
            String refreshToken = tokenService.generateRefreshToken(userDetails.id(), userDetails.getRolesAsString());

            return new TokenPair(accessToken, refreshToken);

        } catch (DisabledException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Xác thực Google thất bại: " + e.getMessage(), e);
        }
    }
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
        User newUser =  User.builder()
                .email(request.email())
                .passwordHash(passwordEncoder.encode(request.password()))
                .fullName(request.fullName())
                .isEnabled(Boolean.TRUE)
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .roles(Set.of(userRole))
                .build();
        userRepository.save(newUser);

        SyncUserDto syncUserDto=new SyncUserDto(newUser);
        chatRealtimeService.syncUserToChatService(syncUserDto);

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
        if (refreshToken == null || refreshToken.isEmpty()) return;
        tokenService.deleteRefreshToken(refreshToken);
    }


}
