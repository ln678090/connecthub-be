package org.ln678090.connecthub.chat.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ln678090.connecthub.auth.config.jwt.TokenService;
import org.ln678090.connecthub.auth.config.keys.RsaKeyProperties;
import org.ln678090.connecthub.auth.entity.User;
import org.ln678090.connecthub.auth.service.UserService;
import org.ln678090.connecthub.chat.dto.SyncUserDto;
import org.ln678090.connecthub.chat.dto.UpdateProfileSyncDto;
import org.ln678090.connecthub.chat.key.ChatRealTimebUrl;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Base64;
import java.util.Map;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Service
public class ChatRealtimeService {
    private final RestClient restClient;
    private final ChatRealTimebUrl chatRealTimebUrl;

    private final TokenService tokenService;

    public void syncUserToChatService(SyncUserDto user) {
        try {
            // Định nghĩa payload giống với SyncUserDto bên Chat


            String token = tokenService.generateInternalToken(user.getId());

            // Chỉ truyền path, RestClient sẽ tự động nối với baseUrl (http://localhost:8808)
            restClient.post()
                    .uri("/api/internal/users/sync")
                    .body(user)
                    .header("Authorization", "Bearer " + token)
                    .retrieve()
                    .toBodilessEntity();

            log.info("Đồng bộ user {} sang ChatRealTime thành công!", user.getId());
        } catch (Exception e) {
            // Chỉ log lỗi, không throw exception để tránh việc đăng ký ở ConnectHub bị rollback
            log.error("Lỗi khi đồng bộ user sang ChatRealTime: {}", e.getMessage());
        }
    }
    public void syncUpdateProfileToChatService(UUID userId, String fullName, String avatarUrl, String location) {
        try {
            String token = tokenService.generateInternalToken(userId);


            UpdateProfileSyncDto dto = UpdateProfileSyncDto.builder()
                    .id(userId)
                    .fullName(fullName)
                    .avatar(avatarUrl)
                    .address(location)
                    .build();

            restClient.put()
                    .uri("/api/internal/users/sync/profile")
                    .body(dto)
                    .header("Authorization", "Bearer " + token)
                    .retrieve()
                    .toBodilessEntity();

            log.info("Đồng bộ profile user {} sang ChatRealTime thành công!", userId);
        } catch (Exception e) {
            // Không throw để tránh rollback luồng chính
            log.error("Lỗi khi đồng bộ profile sang ChatRealTime: {}", e.getMessage());
        }
    }
}
