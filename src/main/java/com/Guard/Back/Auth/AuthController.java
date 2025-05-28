// AuthController.java - Refresh Token을 통해 Access Token 재발급
package com.Guard.Back.Auth;

import com.Guard.Back.Domain.RefreshToken;
import com.Guard.Back.Domain.User;
import com.Guard.Back.Jwt.JwtTokenProvider;
import com.Guard.Back.Repository.RefreshTokenRepository;
import com.Guard.Back.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;

    // Refresh Token으로 Access Token 재발급
    @PostMapping("/refresh")
    public ResponseEntity<?> refreshAccessToken(@RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader) {

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.badRequest().body("Refresh token이 없습니다.");
        }

        String refreshToken = authHeader.substring(7);

        // 1. DB에서 토큰 존재 여부 확인
        Optional<RefreshToken> optionalToken = refreshTokenRepository.findByToken(refreshToken);
        if (optionalToken.isEmpty()) {
            return ResponseEntity.status(401).body("유효하지 않은 refresh token입니다.");
        }

        RefreshToken storedToken = optionalToken.get();
        if (storedToken.isRevoked() || storedToken.getExpiryDate().isBefore(java.time.LocalDateTime.now())) {
            return ResponseEntity.status(401).body("만료되었거나 취소된 refresh token입니다.");
        }

        // 2. 유효한 경우, access token 새로 발급
        User user = storedToken.getUser();
        String newAccessToken = jwtTokenProvider.createAccessToken(user.getId(), user.getNickname());

        return ResponseEntity.ok(new TokenRefreshResponse(newAccessToken, user.getNickname()));
    }

    // 응답 DTO
    record TokenRefreshResponse(String accessToken, String nickname) {}
}
