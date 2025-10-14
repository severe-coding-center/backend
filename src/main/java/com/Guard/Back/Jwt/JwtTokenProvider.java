package com.Guard.Back.Jwt;

import com.Guard.Back.Domain.UserRole; // 💡 import 추가
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

@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String secretKey;
    private Key key;
    private final long ACCESS_TOKEN_VALID_TIME = 1000L * 60 * 30;
    private final long REFRESH_TOKEN_VALID_TIME = 1000L * 60 * 60 * 24 * 30;

    @PostConstruct
    protected void init() {
        this.key = Keys.hmacShaKeyFor(secretKey.getBytes());
    }

    /**
     * 💡 [수정] userType(String) 대신 userRole(UserRole)을 파라미터로 받습니다.
     */
    public String createAccessToken(Long userId, UserRole userRole) {
        Claims claims = Jwts.claims().setSubject(String.valueOf(userId));
        // 💡 [수정] "userType" 대신 "role"이라는 키로 "ROLE_" 접두사가 붙은 값을 저장합니다.
        claims.put("role", userRole.getKey());

        Date now = new Date();
        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + ACCESS_TOKEN_VALID_TIME))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public String createRefreshToken() {
        Date now = new Date();
        return Jwts.builder()
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + REFRESH_TOKEN_VALID_TIME))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * 💡 [수정] 토큰에서 "role" 정보를 직접 읽어 Authentication 객체를 생성합니다.
     */
    public Authentication getAuthentication(String token) {
        Claims claims = parseClaims(token);
        String role = claims.get("role", String.class);

        // 💡 [수정] principal(사용자 정보)에는 ID, credentials(자격증명)은 null, authorities(권한)에 역할을 담습니다.
        return new UsernamePasswordAuthenticationToken(
                claims.getSubject(), // Principal: 사용자 ID (String)
                null,              // Credentials: 비밀번호는 사용하지 않으므로 null
                Collections.singleton(new SimpleGrantedAuthority(role)) // Authorities: 권한 정보
        );
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public Long getUserId(String token) {
        return Long.parseLong(parseClaims(token).getSubject());
    }

    // 💡 [수정] 토큰을 한 번만 파싱하도록 private 헬퍼 메소드로 추출
    private Claims parseClaims(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build()
                .parseClaimsJws(token).getBody();
    }

    // 💡 [삭제] 더 이상 사용되지 않는 getUserType 메소드는 삭제합니다.
    /*
    public String getUserType(String token) {
        return parseClaims(token).get("userType", String.class);
    }
    */
}