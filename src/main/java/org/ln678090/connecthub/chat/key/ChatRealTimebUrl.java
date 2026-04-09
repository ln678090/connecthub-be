package org.ln678090.connecthub.chat.key;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "appbe")
public record ChatRealTimebUrl(
        String chatrealtimeurl

) {}