package org.ln678090.connecthub.post.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.io.Serializable;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Embeddable
public class PostLikeId implements Serializable {
    private static final long serialVersionUID = -5685343627681864121L;
    @NotNull
    @Column(name = "post_id", nullable = false)
    private UUID postId;

    @NotNull
    @Column(name = "user_id", nullable = false)
    private UUID userId;


}