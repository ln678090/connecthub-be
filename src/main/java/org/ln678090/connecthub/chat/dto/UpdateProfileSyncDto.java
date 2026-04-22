package org.ln678090.connecthub.chat.dto;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Builder
public class UpdateProfileSyncDto {
    private UUID id;
    private String fullName;
    private String avatar;
    private String address; // map từ location
}