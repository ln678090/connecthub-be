package org.ln678090.connecthub.chat.dto;

import lombok.Getter;
import lombok.Setter;
import org.ln678090.connecthub.auth.entity.User;

import java.util.UUID;
@Getter
@Setter
public class SyncUserDto{
     UUID id;
     String username;
     String fullName;
     String email;
     String avatar;

    public SyncUserDto(User newUser) {
        this.id=newUser.getId();
        this.username=newUser.getUsername();
        this.fullName=newUser.getFullName();
        this.email=newUser.getEmail();
        this.avatar=newUser.getAvatarUrl();
    }
}