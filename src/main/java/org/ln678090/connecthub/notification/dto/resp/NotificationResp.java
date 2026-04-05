package org.ln678090.connecthub.notification.dto.resp;

import java.time.OffsetDateTime;
import java.util.UUID;




public interface NotificationResp {
    UUID getId();
    String getType();
    String getReferenceId();
    Boolean getIsRead();
    Integer getActorCount();
    OffsetDateTime getUpdatedAt();

    // Nested interface để lấy thông tin actor
    ActorInfo getActor();

    interface ActorInfo {
        UUID getId();
        String getFullName();
        String getAvatarUrl();
    }
}