package org.ln678090.connecthub.auth.service;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.ln678090.connecthub.auth.dto.resp.UserProfileResp;

import java.util.UUID;

public interface UserService {
    void updateAvatar(UUID id, String avatarUrl);

    void updateCover(UUID id, String coverUrl);

    UserProfileResp getProfile(String id);

    // UserServiceImpl.java
    UserProfileResp getProfile(UUID currentUserId, String targetIdStr);

    void updateProfileDetails(UUID currentUserId, @NotBlank(message = "Họ tên không được để trống") @Size(max = 100, message = "Họ tên không được vượt quá 100 ký tự") String s, @Size(max = 255, message = "Tiểu sử không được vượt quá 255 ký tự") String bio, @Size(max = 100, message = "Vị trí không được vượt quá 100 ký tự") String location, @Size(max = 500, message = "URL trang web không được vượt quá 500 ký tự") String s1);
}
