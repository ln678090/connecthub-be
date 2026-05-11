package org.ln678090.connecthub.tracking.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.ln678090.connecthub.auth.utils.SecurityUtils;
import org.ln678090.connecthub.common.dto.resp.ApiResp;
import org.ln678090.connecthub.tracking.entity.UserAction;
import org.ln678090.connecthub.tracking.kafka.UserActionProducer;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.OffsetDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/api/tracking")
@RequiredArgsConstructor
public class TrackingController {
    private final UserActionProducer userActionProducer;

    @PostMapping("/action")
    public ApiResp<Object> trackAction(
            @RequestBody UserAction req,
    Authentication authentication,
            HttpServletRequest  request
    ) {
        UUID currentUserId = (authentication != null) ? SecurityUtils.currentUserId(authentication) : null;
        req.setUserId(currentUserId);
        req.setIpAddress(request.getRemoteAddr());
        req.setCreatedAt(OffsetDateTime.now());

        userActionProducer.sendAction(req);

        return ApiResp.builder().message("Tracked").build();
    }
}
