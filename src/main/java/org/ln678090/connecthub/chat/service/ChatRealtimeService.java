package org.ln678090.connecthub.chat.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ln678090.connecthub.auth.config.keys.RsaKeyProperties;
import org.ln678090.connecthub.auth.entity.User;
import org.ln678090.connecthub.auth.service.UserService;
import org.ln678090.connecthub.chat.dto.SyncUserDto;
import org.ln678090.connecthub.chat.key.ChatRealTimebUrl;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Base64;
import java.util.Map;
@Slf4j
@RequiredArgsConstructor
@Service
public class ChatRealtimeService {
    private final RestClient restClient;
    private final ChatRealTimebUrl chatRealTimebUrl;
    private final RsaKeyProperties rsaKeyProperties;
    public void syncUserToChatService(SyncUserDto user) {
        try {
            // Định nghĩa payload giống với SyncUserDto bên Chat

            String secretHeader = Base64.getEncoder().encodeToString(rsaKeyProperties.privateKey().getEncoded());

            // Chỉ truyền path, RestClient sẽ tự động nối với baseUrl (http://localhost:8808)
            restClient.post()
                    .uri("/api/internal/users/sync")
                    .body(user)
                    .header("X-Internal-Secret", secretHeader)
                    .retrieve()
                    .toBodilessEntity();

            log.info("Đồng bộ user {} sang ChatRealTime thành công!", user.getId());
        } catch (Exception e) {
            // Chỉ log lỗi, không throw exception để tránh việc đăng ký ở ConnectHub bị rollback
            log.error("Lỗi khi đồng bộ user sang ChatRealTime: {}", e.getMessage());
        }
    }
}
