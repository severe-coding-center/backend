package com.Guard.Back.Jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;

/**
 * 모든 HTTP 요청에 대해 JWT 토큰을 검사하는 필터.
 * Spring Security의 기본 인증 필터보다 먼저 실행됩니다.
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // 1. 요청 헤더에서 "Authorization" 값을 가져옵니다.
        String header = request.getHeader("Authorization");

        // 2. 토큰이 존재하며 "Bearer "로 시작하는지 확인합니다.
        if (header != null && header.startsWith("Bearer ")) {
            String accessToken = header.substring(7); // "Bearer " 접두사 제거

            // 3. 토큰이 유효한지 검증합니다.
            if (jwtTokenProvider.validateToken(accessToken)) {
                // 4. 토큰이 유효하면, 사용자 정보를 담은 Authentication 객체를 생성합니다.
                Authentication authentication = jwtTokenProvider.getAuthentication(accessToken);
                // 5. 생성된 Authentication 객체를 SecurityContextHolder에 저장합니다.
                //    이를 통해 현재 요청을 처리하는 동안 사용자가 인증된 상태임을 유지합니다.
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }

        // 6. 다음 필터로 요청을 전달합니다.
        filterChain.doFilter(request, response);
    }
}