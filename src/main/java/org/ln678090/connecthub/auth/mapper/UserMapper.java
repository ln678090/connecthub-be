package org.ln678090.connecthub.auth.mapper;

import org.ln678090.connecthub.auth.dto.resp.UserProfileResp;
import org.ln678090.connecthub.auth.entity.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {

     UserProfileResp toUserProfileResp(User user);
}
