package org.ln678090.connecthub.friend.exception;

import lombok.extern.slf4j.Slf4j;
import org.ln678090.connecthub.common.dto.resp.ApiResp;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

@Slf4j
@RestControllerAdvice(basePackages = "org.ln678090.connecthub.friend") // Thu hẹp
@Order(Ordered.HIGHEST_PRECEDENCE)
public class FriendExceptionHandler {
    @ExceptionHandler(FriendLogicException.class)
    public ResponseEntity<ApiResp<Void>> handleFriendLogicException(FriendLogicException ex) {
        log.warn("Friend logic error: {}", ex.getMessage());
        ApiResp<Void> apiResp = ApiResp.<Void>builder()
                .message(ex.getMessage())
                .timestamp(Instant.now().toString())
                .build();
        return ResponseEntity.status(HttpStatus.CONFLICT).body(apiResp);
    }
}
