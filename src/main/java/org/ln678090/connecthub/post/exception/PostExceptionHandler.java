package org.ln678090.connecthub.post.exception;

import lombok.extern.slf4j.Slf4j;
import org.ln678090.connecthub.auth.dto.resp.ApiResp;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

@Slf4j
@RestControllerAdvice(basePackages = "org.ln678090.connecthub.post") // Thu hẹp phạm vi
@Order(Ordered.HIGHEST_PRECEDENCE)
public class PostExceptionHandler {

    @ExceptionHandler(PostNotFoundException.class)
    public ResponseEntity<ApiResp<Void>> handlePostNotFoundException(PostNotFoundException ex) {
        log.warn("Post not found: {}", ex.getMessage());
        ApiResp<Void> apiResp = ApiResp.<Void>builder()
                .message(ex.getMessage())
                .timestamp(Instant.now().toString())
                .build();
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(apiResp);
    }
}