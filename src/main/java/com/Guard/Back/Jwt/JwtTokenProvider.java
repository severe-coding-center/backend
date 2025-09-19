package com.Guard.Back.Jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import java.security.Key;
import java.util.Collections;
import java.util.Date;

/**
 * JWT(Access Token, Refresh Token)의 생성, 검증, 정보 추출을 담당하는 클래스.
 */
@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String secretKey;
    private Key key;
    private final long ACCESS_TOKEN_VALID_TIME = 1000L * 60 * 30; // 30분
    private final long REFRESH_TOKEN_VALID_TIME = 1000L * 60 * 60 * 24 * 7; // 7일

    /**
     * 객체 초기화 시, secretKey를 Base64로 디코딩하여 Key 객체를 생성합니다.
     */
    @PostConstruct
    protected void init() {
        this.key = Keys.hmacShaKeyFor(secretKey.getBytes());
    }

    /**
     * Access Token을 생성합니다.
     * @param userId 토큰의 주인이 될 사용자의 ID
     * @param userType 사용자의 타입 ("GUARDIAN" 또는 "PROTECTED")
     * @return 생성된 Access Token 문자열
     */
    public String createAccessToken(Long userId, String userType) {
        // 토큰에 담을 정보(Claims) 설정
        Claims claims = Jwts.claims().setSubject(String.valueOf(userId));
        claims.put("userType", userType); // 사용자 타입을 추가하여 API 호출 시 권한 검증에 활용 가능

        Date now = new Date();
        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + ACCESS_TOKEN_VALID_TIME))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Refresh Token을 생성합니다.
     * @return 생성된 Refresh Token 문자열
     */
    public String createRefreshToken() {
        Date now = new Date();
        return Jwts.builder()
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + REFRESH_TOKEN_VALID_TIME))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * 유효한 토큰에서 Authentication 객체를 생성하여 반환합니다.
     * 이 객체는 Spring Security가 사용자를 인증하는 데 사용됩니다.
     * @param token 검증된 Access Token
     * @return Authentication 객체
     */
    public Authentication getAuthentication(String token) {
        Long userId = getUserId(token);
        String userType = getUserType(token);
        // Principal: 사용자 식별자(ID), Credentials: 사용자 타입(권한 검증용)
        return new UsernamePasswordAuthenticationToken(userId, userType,
                Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")));
    }

    /**
     * 토큰의 유효성 및 만료일자를 검증합니다.
     * @param token 검증할 토큰
     * @return 유효하면 true, 아니면 false
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            // 유효하지 않은 토큰(서명 오류, 만료 등)일 경우 false 반환
            return false;
        }
    }

    /**
     * 토큰에서 사용자 ID를 추출합니다.
     * @param token 정보를 추출할 토큰
     * @return 사용자 ID
     */
    public Long getUserId(String token) {
        return Long.parseLong(Jwts.parserBuilder().setSigningKey(key).build()
                .parseClaimsJws(token).getBody().getSubject());
    }

    /**
     * 토큰에서 사용자 타입을 추출합니다.
     * @param token 정보를 추출할 토큰
     * @return 사용자 타입 ("GUARDIAN" 또는 "PROTECTED")
     */
    public String getUserType(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build()
                .parseClaimsJws(token).getBody().get("userType", String.class);
    }
}