package org.ln678090.connecthub.post.controller;

import com.cloudinary.Cloudinary;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/cloudinary")
@RequiredArgsConstructor
public class CloudinaryController {

    private final Cloudinary cloudinary;

    @PostMapping("/signature")
    public ResponseEntity<Map<String, Object>> generateSignature(
            @RequestBody Map<String, Object> req
    ) {
        // NẾU REQ KHÔNG CÓ TIMESTAMP (Direct Upload), BE PHẢI TỰ THÊM VÀO TRƯỚC KHI KÝ
        if (!req.containsKey("timestamp")) {
            req.put("timestamp", String.valueOf(System.currentTimeMillis() / 1000));
        }

        // Ký vào Map
        cloudinary.signRequest(req, new HashMap<>());


        return ResponseEntity.ok(Map.of(
                "signature", String.valueOf(req.get("signature")),
                "timestamp", String.valueOf(req.get("timestamp"))
        ));
    }
}