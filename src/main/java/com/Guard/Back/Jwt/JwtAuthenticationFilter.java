package com.Guard.Back.Jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j; // 💡 Slf4j 임포트 추가
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;

/**
 * 모든 HTTP 요청에 대해 JWT 토큰의 유효성을 검사하고 사용자를 인증하는 필터.
 * Spring Security의 기본 인증 필터(`UsernamePasswordAuthenticationFilter`)보다 먼저 실행됩니다.
 */
@Component
@RequiredArgsConstructor
@Slf4j // 💡 로깅을 위한 어노테이션 추가
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    /**
     * 모든 요청에 대해 한 번씩 실행되는 필터링 메소드.
     * 요청 헤더에서 JWT 토큰을 추출하여 유효성을 검증하고,
     * 유효한 경우 Spring Security 컨텍스트에 인증 정보를 설정합니다.
     *
     * @param request      HTTP 요청 객체
     * @param response     HTTP 응답 객체
     * @param filterChain  다음 필터를 호출하기 위한 체인 객체
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
                //    이를 통해 현재 요청을 처리하는 동안 사용자가 인증된 상태임을 유지합니다.
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } else {
                log.warn("[JWT 필터] 유효하지 않은 토큰으로 접근이 시도되었습니다. URI: {}", request.getRequestURI());
            }
        }

        // 6. 다음 필터로 요청을 전달합니다.
        filterChain.doFilter(request, response);
    }
}