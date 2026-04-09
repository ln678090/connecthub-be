package org.ln678090.connecthub.chat.config;

import org.ln678090.connecthub.chat.key.ChatRealTimebUrl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;


@Configuration
public class RestClientConfigChat {

    private final ChatRealTimebUrl chatRealTimebUrl;

    public RestClientConfigChat(ChatRealTimebUrl chatRealTimebUrl) {
        this.chatRealTimebUrl = chatRealTimebUrl;
    }

    @Bean
    public RestClient restClient() {
        // Khởi tạo RestClient với Base URL là URL của ChatRealTime (ví dụ http://localhost:8808)
        return RestClient.builder()
                .baseUrl(chatRealTimebUrl.chatrealtimeurl())
                .build();
    }
}