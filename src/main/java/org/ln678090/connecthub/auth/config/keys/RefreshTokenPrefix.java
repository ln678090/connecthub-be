package org.ln678090.connecthub.auth.config.keys;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "refresh-token")
public record RefreshTokenPrefix(String key) {
}
