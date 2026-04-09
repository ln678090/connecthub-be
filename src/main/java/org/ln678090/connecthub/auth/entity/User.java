package org.ln678090.connecthub.auth.entity;

import com.github.f4b6a3.uuid.UuidCreator;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;

import java.time.OffsetDateTime;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Builder
@Table(name = "users")
public class User {
    @Id
    @Column
    private UUID id;


    @PrePersist
    final void generateId() {
        if (id == null) {
            id = UuidCreator.getTimeOrderedEpoch();
        }
    }

    @Column(nullable = false)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "full_name", nullable = false, length = 100)
    private String fullName;
    @Builder.Default
    @Column(name = "avatar_url", length = 500)
    private String avatarUrl = "https://res.cloudinary.com/dayoanitt/image/upload/v1774417116/davbhywnemftongrmdwx.jpg";

    @ColumnDefault("true")
    @Column(name = "is_enabled")
    @Builder.Default
    private Boolean isEnabled = Boolean.TRUE;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"), // Khóa ngoại trỏ đến bảng Users
            inverseJoinColumns = @JoinColumn(name = "role_id") // Khóa ngoại trỏ đến bảng Roles
    )
    private Set<Role> roles = new LinkedHashSet<>();
    @Size(max = 255)
    @Builder.Default
    @ColumnDefault("https://res.cloudinary.com/dayoanitt/image/upload/v1774417246/ydts7bqldo4rdl4izki8.jpg")
    @Column(name = "cover_url")
    private String coverUrl = "https://res.cloudinary.com/dayoanitt/image/upload/v1774417246/ydts7bqldo4rdl4izki8.jpg";
    @Column(length = Integer.MAX_VALUE)
    private String bio;
    @Size(max = 100)
    @Column(length = 100)
    private String location;
    @Size(max = 255)
    @Column(name = "website_url")
    private String websiteUrl;
    @Size(max = 255)
    @Column(name = "username")
    private String username;


}