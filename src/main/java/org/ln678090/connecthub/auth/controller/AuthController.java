package org.ln678090.connecthub.auth.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.ln678090.connecthub.auth.dto.privateDto.TokenPair;
import org.ln678090.connecthub.auth.dto.req.LoginRequest;
import org.ln678090.connecthub.auth.dto.req.RegisterRequest;
import org.ln678090.connecthub.common.dto.resp.ApiResp;
import org.ln678090.connecthub.auth.dto.resp.AuthResponse;
import org.ln678090.connecthub.auth.service.AuthService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final String REFRESH_TOKEN_REQUEST_BODY = "refreshToken";
    private final AuthService authService;
    private static final int REFRESH_TOKEN_MAX_AGE = 2592000; //  ngày

    @PostMapping("/login")
    public ResponseEntity<ApiResp<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        TokenPair tokens = authService.login(request);
        return buildAuthResponse(tokens, HttpStatus.OK, "Đăng nhập thành công", false);
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResp<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {
        TokenPair tokens = authService.register(request);
        return buildAuthResponse(tokens, HttpStatus.CREATED, "Đăng ký thành công", false);
    }

    @PostMapping("/login-mobile")
    public ResponseEntity<ApiResp<AuthResponse>> loginMobile(@Valid @RequestBody LoginRequest request) {
        TokenPair tokens = authService.login(request);
        return buildAuthResponse(tokens, HttpStatus.OK, "Đăng nhập thành công", true);
    }

    @PostMapping("/register-mobile")
    public ResponseEntity<ApiResp<AuthResponse>> registerMobile(@Valid @RequestBody RegisterRequest request) {
        TokenPair tokens = authService.register(request);
        return buildAuthResponse(tokens, HttpStatus.CREATED, "Đăng ký thành công", true);
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResp<AuthResponse>> refresh(
            @CookieValue(name = "refresh_token", required = false) String cookieRefreshToken
    ) {
        if (cookieRefreshToken == null || cookieRefreshToken.isEmpty()) {
            throw new org.springframework.security.authentication.BadCredentialsException("Không tìm thấy Refresh Token trong Cookie");
        }
        TokenPair newTokens = authService.refreshToken(cookieRefreshToken);
        return buildAuthResponse(newTokens, HttpStatus.OK, "Làm mới Token thành công", false);
    }

    @PostMapping("/refresh-mobile")
    public ResponseEntity<ApiResp<AuthResponse>> refreshMobile(
            @RequestBody(required = false) Map<String, String> body
    ) {
        String refreshToken = body != null && body.containsKey(REFRESH_TOKEN_REQUEST_BODY)
                ? body.get(REFRESH_TOKEN_REQUEST_BODY)
                : null;
        if (refreshToken == null || refreshToken.isEmpty()) {
            throw new org.springframework.security.authentication.BadCredentialsException("Không tìm thấy Refresh Token trong body");
        }
        TokenPair newTokens = authService.refreshToken(refreshToken);
        return buildAuthResponse(newTokens, HttpStatus.OK, "Làm mới Token thành công", true);
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResp<String>> logout(
            @CookieValue(name = "refresh_token", required = false) String cookieRefreshToken,
            @RequestBody(required = false) Map<String, String> body
    ) {
        String refreshToken = body != null && body.containsKey(REFRESH_TOKEN_REQUEST_BODY)
                ? body.get(REFRESH_TOKEN_REQUEST_BODY)
                : cookieRefreshToken;
        authService.logout(refreshToken);
        ResponseCookie deleteCookie;
        if(!(body != null && body.containsKey(REFRESH_TOKEN_REQUEST_BODY))){
            deleteCookie = ResponseCookie.from("refresh_token", "")
                    .httpOnly(true)
                    .secure(false)
                    .path("/api/auth")
                    .maxAge(0)
                    .sameSite("None")
                    .build();
        } else {
            deleteCookie = null;
        }

        ApiResp<String> response = ApiResp.<String>builder()
                .message("Đăng xuất thành công")
                .data("OK")
                .timestamp(Instant.now().toString())
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, deleteCookie != null ? deleteCookie.toString() : "")
                .body(response);
    }

    @GetMapping("/test")
    public ResponseEntity<ApiResp<String>> test() {

//        String threadName = Thread.currentThread().getName();

        // Log ra console để bạn quan sát trực tiếp trên Render Log
//        System.out.println("===> Request đang được xử lý bởi Thread: " + threadName);

        return ResponseEntity.ok(ApiResp.<String>builder()
//                .timestamp(Instant.now().toString())
//                .data("Current Thread: " + threadName)
                .build());
    }

    /**
     * Hàm tiện ích DUY NHẤT để gom chung logic tạo Cookie và bọc ApiResp
     */
    private ResponseEntity<ApiResp<AuthResponse>> buildAuthResponse(
            TokenPair tokens, HttpStatus status, String message, boolean isMobile) {

        ResponseCookie refreshCookie = ResponseCookie.from("refresh_token", tokens.refreshToken())
                .httpOnly(true)
                .secure(true) // production nên để true khi dùng HTTPS
                .path("/api/auth")
                .maxAge(REFRESH_TOKEN_MAX_AGE)
                .sameSite("None")
                .build();

        AuthResponse authResponse = new AuthResponse(
                tokens.accessToken(),
                isMobile ? tokens.refreshToken() : null,
                "Bearer"
        );

        ApiResp<AuthResponse> apiResp = ApiResp.<AuthResponse>builder()
                .message(message)
                .data(authResponse)
                .timestamp(Instant.now().toString())
                .build();

        ResponseEntity.BodyBuilder responseBuilder = ResponseEntity.status(status);

        if (!isMobile) {
            responseBuilder.header(HttpHeaders.SET_COOKIE, refreshCookie.toString());
        }

        return responseBuilder.body(apiResp);
    }
}
