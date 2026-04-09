package org.ln678090.connecthub.auth.repository;

import org.ln678090.connecthub.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    boolean existsByEmail(String email);

   Optional <User> findByEmail(String email);

    List<User> findTop10ByIdNotOrderByCreatedAtDesc(UUID id);

    @Query("SELECT u FROM User u WHERE " +
            "(LOWER(u.fullName) LIKE LOWER(CONCAT('%', :query, '%')) " +
            "OR LOWER(u.username) LIKE LOWER(CONCAT('%', :query, '%'))) " +
            "AND u.id != :currentUserId")
    List<User> searchUsers(@Param("query") String query, @Param("currentUserId") UUID currentUserId);
}