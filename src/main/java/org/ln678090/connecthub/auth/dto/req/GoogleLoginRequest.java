package org.ln678090.connecthub.auth.dto.req;

import jakarta.validation.constraints.NotBlank;

public record GoogleLoginRequest(
        @NotBlank(message = "Google Token không được để trống")
        String idToken
) {}