package org.ln678090.connecthub.auth.mapper;

import javax.annotation.processing.Generated;
import org.ln678090.connecthub.auth.dto.resp.UserProfileResp;
import org.ln678090.connecthub.auth.entity.User;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-04-22T15:57:13+0700",
    comments = "version: 1.6.3, compiler: javac, environment: Java 25.0.1 (Oracle Corporation)"
)
@Component
public class UserMapperImpl implements UserMapper {

    @Override
    public UserProfileResp toUserProfileResp(User user) {
        if ( user == null ) {
            return null;
        }

        String fullName = null;
        String bio = null;
        String location = null;
        String websiteUrl = null;
        String avatarUrl = null;
        String coverUrl = null;

        fullName = user.getFullName();
        bio = user.getBio();
        location = user.getLocation();
        websiteUrl = user.getWebsiteUrl();
        avatarUrl = user.getAvatarUrl();
        coverUrl = user.getCoverUrl();

        String friendshipStatus = null;
        long followerCount = 0L;
        long followingCount = 0L;
        boolean isFollowing = false;
        boolean isOnline = false;

        UserProfileResp userProfileResp = new UserProfileResp( fullName, bio, location, websiteUrl, avatarUrl, coverUrl, friendshipStatus, followerCount, followingCount, isFollowing, isOnline );

        return userProfileResp;
    }
}
