package com.Guard.Back.Jwt;

import com.Guard.Back.Domain.UserRole;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j; // 💡 Slf4j 임포트 추가
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import java.security.Key;
import java.util.Collections;
import java.util.Date;

/*JWT(Access Token, Refresh Token)의 생성, 검증, 정보 추출을 담당하는 핵심 클래스.*/
@Component
@RequiredArgsConstructor
@Slf4j // 💡 로깅을 위한 어노테이션 추가
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String secretKey;
    private Key key;
    private final long ACCESS_TOKEN_VALID_TIME = 1000L * 60 * 30; // 30분
    private final long REFRESH_TOKEN_VALID_TIME = 1000L * 60 * 60 * 24 * 30; // 30일

    /**
     * 객체 초기화 시, application.properties의 secretKey를 사용하여 HMAC-SHA 키 객체를 생성합니다.
     * 이 키는 모든 토큰의 서명에 사용됩니다.
     */
    @PostConstruct
    protected void init() {
        this.key = Keys.hmacShaKeyFor(secretKey.getBytes());
    }

    /**
     * Access Token을 생성합니다.
     * @param userId   토큰의 주인이 될 사용자의 ID.
     * @param userRole 사용자의 역할 (GUARDIAN 또는 PROTECTED).
     * @return 생성된 Access Token 문자열.
     */
    public String createAccessToken(Long userId, UserRole userRole) {
        Claims claims = Jwts.claims().setSubject(String.valueOf(userId));
        claims.put("role", userRole.getKey()); // "ROLE_GUARDIAN" 또는 "ROLE_PROTECTED"

        Date now = new Date();
        String accessToken = Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + ACCESS_TOKEN_VALID_TIME))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
        log.info("[토큰 생성] 사용자 ID: {}, 역할: {}의 Access Token이 생성되었습니다.", userId, userRole.name());
        return accessToken;
    }

    /**
     * Refresh Token을 생성합니다.
     * Refresh Token에는 사용자 정보를 담지 않아 보안을 강화합니다.
     * @return 생성된 Refresh Token 문자열.
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
     * 유효한 Access Token에서 Spring Security가 사용할 Authentication 객체를 생성하여 반환합니다.
     * @param token 검증된 Access Token.
     * @return 사용자의 ID와 권한 정보가 담긴 Authentication 객체.
     */
    public Authentication getAuthentication(String token) {
        Claims claims = parseClaims(token);
        String role = claims.get("role", String.class);

        return new UsernamePasswordAuthenticationToken(
                claims.getSubject(), // Principal: 사용자 ID (String)
                null,              // Credentials: 사용하지 않음
                Collections.singleton(new SimpleGrantedAuthority(role)) // Authorities: 권한 정보
        );
    }

    /**
     * 토큰의 유효성 및 만료일자를 검증합니다.
     * @param token 검증할 토큰.
     * @return 유효하면 true, 아니면 false.
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("[토큰 검증 실패] 유효하지 않은 토큰입니다. 에러: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 토큰에서 사용자 ID(subject)를 추출합니다.
     * @param token 정보를 추출할 토큰.
     * @return 사용자 ID (Long).
     */
    public Long getUserId(String token) {
        return Long.parseLong(parseClaims(token).getSubject());
    }

    /**
     * 토큰을 파싱하여 클레임(토큰에 담긴 정보)을 추출하는 private 헬퍼 메소드.
     * 코드 중복을 방지하고 일관된 파싱 로직을 제공합니다.
     * @param token 파싱할 토큰.
     * @return 토큰의 클레임 정보.
     */
    private Claims parseClaims(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build()
                .parseClaimsJws(token).getBody();
    }
}