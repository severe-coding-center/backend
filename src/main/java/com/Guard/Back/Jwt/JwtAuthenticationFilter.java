package com.Guard.Back.Jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse; // 👈 누락된 import 추가
import lombok.RequiredArgsConstructor;       // 👈 누락된 import 추가
import lombok.extern.slf4j.Slf4j;           // 👈 누락된 import 추가
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;    // 👈 누락된 import 추가
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;

/**
 * 모든 HTTP 요청에 대해 JWT 토큰의 유효성을 검사하고 사용자를 인증하는 필터.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    /**
     * [추가된 로직] 이 필터가 실행되면 안 되는 경로를 지정합니다.
     * SecurityConfig에서 permitAll()로 설정된 공용 경로는 토큰 검사를 건너뜁니다.
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();

        // SecurityConfig와 동일하게 공용 경로를 제외합니다.
        return path.startsWith("/oauth2") ||
                path.startsWith("/api/auth") ||
                path.startsWith("/api/protected/register") ||
                path.startsWith("/login") ||
                path.equals("/favicon.ico");
    }

    /**
     * 모든 요청에 대해 한 번씩 실행되는 필터링 메소드.
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // 1. 요청 헤더에서 "Authorization" 값을 가져옵니다.
        String header = request.getHeader("Authorization");

        // 2. 토큰이 존재하며 "Bearer "로 시작하는지 확인합니다.
        if (header != null && header.startsWith("Bearer ")) {
            String accessToken = header.substring(7); // "Bearer " 접두사 제거
            log.debug("[JWT 필터] Authorization 헤더에서 토큰을 추출했습니다. URI: {}", request.getRequestURI());

            // 3. 토큰이 유효한지 검증합니다.
            if (jwtTokenProvider.validateToken(accessToken)) {
                log.debug("[JWT 필터] 토큰이 유효합니다. 인증 정보를 SecurityContext에 설정합니다.");
                // 4. 토큰이 유효하면, 사용자 정보를 담은 Authentication 객체를 생성합니다.
                Authentication authentication = jwtTokenProvider.getAuthentication(accessToken);
                // 5. 생성된 Authentication 객체를 SecurityContextHolder에 저장합니다.
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } else {
                log.warn("[JWT 필터] 유효하지 않은 토큰으로 접근이 시도되었습니다. URI: {}", request.getRequestURI());
            }
        }

        // 6. 다음 필터로 요청을 전달합니다.
        filterChain.doFilter(request, response);
    }
}