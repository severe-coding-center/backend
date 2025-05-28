// JwtTokenProvider.java - JWT 생성과 검증을 담당하는 클래스
package com.Guard.Back.Jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.security.Key;
import java.util.Date;

@Component
public class JwtTokenProvider {

    // application.properties에서 가져올 비밀 키
    @Value("${jwt.secret}")
    private String secretKey;

    private Key key;

    // 토큰 유효 시간 설정: Access Token은 30분, Refresh Token은 7일
    private final long ACCESS_TOKEN_VALID_TIME = 1000L * 60 * 30;
    private final long REFRESH_TOKEN_VALID_TIME = 1000L * 60 * 60 * 24 * 7;

    // 초기화 시점에 비밀키를 Key 객체로 변환
    @PostConstruct
    protected void init() {
        this.key = Keys.hmacShaKeyFor(secretKey.getBytes());
    }

    // Access Token 생성 메서드
    public String createAccessToken(Long userId, String nickname) {
        return Jwts.builder()
                .setSubject("AccessToken")
                .claim("userId", userId)
                .claim("nickname", nickname)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + ACCESS_TOKEN_VALID_TIME))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    // Refresh Token 생성 메서드
    public String createRefreshToken() {
        return Jwts.builder()
                .setSubject("RefreshToken")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + REFRESH_TOKEN_VALID_TIME))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    // 토큰 유효성 검사
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    // 토큰에서 userId 추출
    public Long getUserId(String token) {
        Claims claims = Jwts.parserBuilder().setSigningKey(key).build()
                .parseClaimsJws(token)
                .getBody();
        return claims.get("userId", Long.class);
    }

    // 토큰에서 nickname 추출
    public String getNickname(String token) {
        Claims claims = Jwts.parserBuilder().setSigningKey(key).build()
                .parseClaimsJws(token)
                .getBody();
        return claims.get("nickname", String.class);
    }
}
