package org.ln678090.connecthub.auth.repository;

import org.ln678090.connecthub.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    boolean existsByEmail(String email);

   Optional <User> findByEmail(String email);

    List<User> findTop10ByIdNotOrderByCreatedAtDesc(UUID id);
}