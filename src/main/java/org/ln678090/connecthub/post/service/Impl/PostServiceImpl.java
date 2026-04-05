package org.ln678090.connecthub.post.service.Impl;

import lombok.RequiredArgsConstructor;
import org.ln678090.connecthub.auth.entity.User;
import org.ln678090.connecthub.auth.repository.UserRepository;
import org.ln678090.connecthub.notification.entity.TypeNotification;
import org.ln678090.connecthub.notification.service.NotificationService;
import org.ln678090.connecthub.post.dto.req.CreatePostRequest;
import org.ln678090.connecthub.post.dto.resp.PostResponse;
import org.ln678090.connecthub.post.entity.Post;
import org.ln678090.connecthub.post.entity.PostLike;
import org.ln678090.connecthub.post.entity.PostLikeId;
import org.ln678090.connecthub.post.exception.ResourceNotFoundException;
import org.ln678090.connecthub.post.repository.PostLikeRepository;
import org.ln678090.connecthub.post.repository.PostRepository;
import org.ln678090.connecthub.post.service.PostService;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.AccessDeniedException;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostServiceImpl implements PostService
{
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final PostLikeRepository postLikeRepository;
    private final NotificationService notificationService;

    @Override
    @Transactional
    public PostResponse createPost(UUID currentUserId, CreatePostRequest request) {
        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy user"));

        Post post = new Post();
        post.setUser(user);
        post.setContent(request.content().trim());
        post.setImageUrl(request.imageUrl());

        Post saved = postRepository.save(post);



        return toResponse(saved, false, 0L);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getFeedCursor(UUID currentUserId, ScrollPosition scrollPosition, int size) {
        // Query Database bằng Scroll API (Keyset)
        Window<Post> postWindow = postRepository.findAllByOrderByCreatedAtDescIdDesc(
                scrollPosition, Limit.of(size));

        // Lấy danh sách UUID của các bài Post trong Window này
        Set<UUID> postIds = postWindow.getContent().stream()
                .map(Post::getId)
                .collect(Collectors.toSet());

        // Lấy danh sách các bài viết User hiện tại đã like
        Set<UUID> likedPostIds = postIds.isEmpty() ? Set.of() :
                postLikeRepository.findByUser_IdAndPost_IdIn(currentUserId, postIds)
                        .stream()
                        .map(like -> like.getPost().getId())
                        .collect(Collectors.toSet());

        // Chuyển đổi từ Entity sang DTO
        List<PostResponse> dtos = postWindow.getContent().stream()
                .map(post -> toResponse(
                        post,
                        likedPostIds.contains(post.getId()),
                        postLikeRepository.countByPost_Id(post.getId())
                )).toList();

        // Xử lý con trỏ KeysetScrollPosition để trả về Map (chứa createdAt và id) cho Client dễ đọc
        Map<String, Object> nextCursorMap = null;
        if (!postWindow.isEmpty() && postWindow.hasNext()) {
            KeysetScrollPosition pos = (KeysetScrollPosition) postWindow.positionAt(postWindow.getContent().size() - 1);
            nextCursorMap = pos.getKeys();
        }

        // Trả về DTO
        return Map.of(
                "posts", dtos,
                "nextCursor", nextCursorMap != null ? nextCursorMap : "",
                "hasNext", postWindow.hasNext()
        );
    }
    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getPostsByUserIdCursor(UUID targetUserId, UUID currentUserId, ScrollPosition scrollPosition, int size) {
        // Query Database bằng Scroll API (lọc theo targetUserId)
        Window<Post> postWindow = postRepository.findByUserIdOrderByCreatedAtDescIdDesc(
                targetUserId, scrollPosition, Limit.of(size));

        // Lấy danh sách UUID của các bài Post
        Set<UUID> postIds = postWindow.getContent().stream()
                .map(Post::getId)
                .collect(Collectors.toSet());

        // Lấy danh sách các bài viết User hiện tại đã like
        Set<UUID> likedPostIds = (currentUserId != null && !postIds.isEmpty())
                ? postLikeRepository.findByUser_IdAndPost_IdIn(currentUserId, postIds)
                .stream()
                .map(like -> like.getPost().getId())
                .collect(Collectors.toSet())
                : Collections.emptySet();

        // Chuyển đổi từ Entity sang DTO
        List<PostResponse> dtos = postWindow.getContent().stream()
                .map(post -> toResponse(
                        post,
                        likedPostIds.contains(post.getId()),
                        postLikeRepository.countByPost_Id(post.getId())
                )).toList();

        // Xử lý con trỏ
        Map<String, Object> nextCursorMap = null;
        if (!postWindow.isEmpty() && postWindow.hasNext()) {
            KeysetScrollPosition pos = (KeysetScrollPosition) postWindow.positionAt(postWindow.getContent().size() - 1);
            nextCursorMap = pos.getKeys();
        }

        return Map.of(
                "posts", dtos,
                "nextCursor", nextCursorMap != null ? nextCursorMap : "",
                "hasNext", postWindow.hasNext()
        );
    }
    @Override
    @Transactional(readOnly = true)
    public Page<PostResponse> getFeed(UUID currentUserId, int page, int size) {
//        Page<Post> posts = postRepository.findAllByOrderByCreatedAtDesc(PageRequest.of(page, size));
//
//        Set<UUID> postIds = posts.getContent().stream().map(Post::getId).collect(Collectors.toSet());
//        Set<UUID> likedPostIds = postLikeRepository.findByUser_IdAndPost_IdIn(currentUserId, postIds)
//                .stream()
//                .map(like -> like.getPost().getId())
//                .collect(Collectors.toSet());

//        return posts.map(post -> toResponse(
//                post,
//                likedPostIds.contains(post.getId()),
//                postLikeRepository.countByPost_Id(post.getId())
//        ));
        return postRepository.getFeedOptimized(currentUserId, PageRequest.of(page, size));
    }

    @Override
    @Transactional
    public void toggleLike(UUID currentUserId, UUID postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Bài viết không tồn tại"));

        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy user"));

        PostLikeId id = new PostLikeId(postId, currentUserId);

        if (postLikeRepository.existsById(id)) {
            postLikeRepository.deleteById(id);
            return;
        }

        PostLike like = new PostLike();
        like.setId(id);
        like.setPost(post);
        like.setUser(user);
        postLikeRepository.save(like);

        notificationService.sendNotification(
                post.getUser().getId(),
                currentUserId,
                TypeNotification.LIKE_POST,
                post.getId().toString()
        );
    }
    @Transactional
    @Override
    public void softDeletePost(UUID postId, UUID currentUserId) throws AccessDeniedException {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Bài viết không tồn tại hoặc đã bị xóa"));

        // Kiểm tra quyền: Chỉ chủ nhân bài viết mới được quyền xóa
        if (!post.getUser().getId().equals(currentUserId)) {
            throw new AccessDeniedException("Bạn không có quyền xóa bài viết này");
        }

        // Thực hiện xóa mềm (Kích hoạt @SQLDelete trong Entity)
        postRepository.delete(post);
    }

    @Override
    @Transactional(readOnly = true)
    public PostResponse getPostById(UUID postId, UUID currentUserId) {

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Bài viết không tồn tại hoặc đã bị xóa"));

        // 2. Kiểm tra xem user hiện tại đã like bài này chưa
        boolean likedByMe = false;
        if (currentUserId != null) {
            likedByMe = postLikeRepository.existsById(new PostLikeId(postId, currentUserId));
        }


        long likeCount = postLikeRepository.countByPost_Id(postId);


        return toResponse(post, likedByMe, likeCount);
    }

    private PostResponse toResponse(Post post, boolean likedByMe, long likeCount) {
        return new PostResponse(
                post.getId(),
                post.getUser().getId(),
                post.getUser().getFullName(),
                post.getUser().getAvatarUrl(),
                post.getContent(),
                post.getImageUrl(),
                post.getCreatedAt(),
                likeCount,
                0L,
                likedByMe
        );
    }
}
