package org.ln678090.connecthub.auth.service.Impl;

import lombok.RequiredArgsConstructor;
import org.ln678090.connecthub.auth.dto.resp.UserProfileResp;
import org.ln678090.connecthub.auth.entity.User;
import org.ln678090.connecthub.auth.mapper.UserMapper;
import org.ln678090.connecthub.auth.repository.UserRepository;
import org.ln678090.connecthub.auth.service.UserService;
import org.ln678090.connecthub.friend.entity.FriendRequestStatus;
import org.ln678090.connecthub.friend.entity.FriendshipId;
import org.ln678090.connecthub.friend.repository.FollowRepository;
import org.ln678090.connecthub.friend.repository.FriendRequestRepository;
import org.ln678090.connecthub.friend.repository.FriendshipRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    final UserRepository userRepository;
    private final UserMapper userMapper;
    private final FriendshipRepository friendshipRepository;
    private final FriendRequestRepository friendRequestRepository;
    private final FollowRepository followRepository;

    @Transactional
    @Override
    public void updateAvatar(UUID id, String avatarUrl) {
        User user = userRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Không tìm thấy user"));
        user.setAvatarUrl(avatarUrl);
        userRepository.save(user);
    }
    @Transactional
    @Override
    public void updateCover(UUID id, String coverUrl) {
        User user = userRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Không tìm thấy user"));
        user.setCoverUrl(coverUrl);
        userRepository.save(user);
    }

    @Override
    public UserProfileResp getProfile(String id) {
        User user=userRepository.findById(UUID.fromString(id)).orElseThrow(()->new IllegalArgumentException("User not found"));
        return userMapper.toUserProfileResp(user);
    }

    @Override
    public UserProfileResp getProfile(UUID currentUserId, String targetIdStr) {
        UUID targetUserId = UUID.fromString(targetIdStr);
        User targetUser = userRepository.findById(targetUserId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        String status = "NONE";

        if (currentUserId != null) {
            if (currentUserId.equals(targetUserId)) {
                status = "SELF";
            } else if (friendshipRepository.existsById(new FriendshipId(currentUserId, targetUserId))) {
                status = "FRIENDS";
            } else if (friendRequestRepository.existsBySenderIdAndReceiverIdAndStatus(currentUserId, targetUserId, FriendRequestStatus.PENDING)) {
                status = "REQUEST_SENT"; // Mình đã gửi
            } else if (friendRequestRepository.existsBySenderIdAndReceiverIdAndStatus(targetUserId, currentUserId, FriendRequestStatus.PENDING)) {
                status = "REQUEST_RECEIVED"; // Người ta gửi cho mình
            }
        }
        long followerCount = followRepository.countByFollowingId(targetUserId);
        long followingCount = followRepository.countByFollowerId(targetUserId);
        boolean isFollowing = currentUserId != null && followRepository.existsByFollowerIdAndFollowingId(currentUserId, targetUserId);


        return new UserProfileResp(
                targetUser.getFullName(), targetUser.getBio(), targetUser.getLocation(),
                targetUser.getWebsiteUrl(), targetUser.getAvatarUrl(), targetUser.getCoverUrl(), status,
                followerCount, followingCount, isFollowing
        );
    }
    @Override
    @Transactional
    public void updateProfileDetails(UUID userId, String fullName, String bio, String location, String websiteUrl) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng"));

        user.setFullName(fullName.trim());
        user.setBio(bio != null ? bio.trim() : null);
        user.setLocation(location != null ? location.trim() : null);
        user.setWebsiteUrl(websiteUrl != null ? websiteUrl.trim() : null);

        userRepository.save(user);
    }
}
