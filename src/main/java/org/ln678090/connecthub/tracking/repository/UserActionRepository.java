package org.ln678090.connecthub.tracking.repository;

import org.ln678090.connecthub.tracking.entity.UserAction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UserActionRepository extends JpaRepository<UserAction, UUID> {
}