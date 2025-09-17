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

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // 1. 요청 헤더에서 "Authorization" 값을 가져옵니다.
        String header = request.getHeader("Authorization");

        // 2. 헤더가 있고 "Bearer "로 시작하는지 확인합니다.
        if (header != null && header.startsWith("Bearer ")) {
            String accessToken = header.substring(7); // "Bearer " 부분을 제외한 토큰만 추출

            // 3. 토큰이 유효한지 검증합니다.
            if (jwtTokenProvider.validateToken(accessToken)) {
                // 4. 토큰이 유효하면, 토큰에서 사용자 정보를 담은 Authentication 객체를 가져옵니다.
                Authentication authentication = jwtTokenProvider.getAuthentication(accessToken);
                // 5. SecurityContext에 Authentication 객체를 저장하여, 현재 사용자가 인증되었음을 알립니다.
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }

        // 다음 필터로 요청을 전달합니다.
        filterChain.doFilter(request, response);
    }
}