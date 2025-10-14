package com.Guard.Back.Config;

import com.Guard.Back.Jwt.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

/**
 * Spring Security의 핵심 설정을 담당하는 클래스.
 * JWT 기반의 인증/인가, CORS, CSRF 등 웹 보안 전반을 설정합니다.
 */
@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    /**
     * Spring Security의 필터 체인을 정의하고 HTTP 보안을 구성합니다.
     *
     * @param http HttpSecurity 객체.
     * @return 구성이 완료된 SecurityFilterChain 객체.
     * @throws Exception 설정 과정에서 발생할 수 있는 예외.
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // CORS(Cross-Origin Resource Sharing) 설정
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                // CSRF(Cross-Site Request Forgery) 보호 비활성화 (JWT 사용 시 불필요)
                .csrf(AbstractHttpConfigurer::disable)
                // 기본 제공되는 로그인 폼 비활성화
                .formLogin(AbstractHttpConfigurer::disable)
                // HTTP Basic 인증 비활성화
                .httpBasic(AbstractHttpConfigurer::disable)
                // 세션을 사용하지 않는 STATELESS 정책 설정 (JWT 기반 인증)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // HTTP 요청에 대한 접근 권한 설정
                .authorizeHttpRequests(auth -> auth
                        // "/api/auth/**", "/api/protected/register" 경로는 인증 없이 누구나 접근 허용
                        .requestMatchers("/api/auth/**", "/api/protected/register").permitAll()
                        // "/api/relationship/link", "/api/location/{...}" 경로는 GUARDIAN 역할만 접근 허용
                        .requestMatchers("/api/relationship/link", "/api/location/{protectedUserId}").hasRole("GUARDIAN")
                        // "/api/location" (POST) 경로는 PROTECTED 역할만 접근 허용
                        .requestMatchers("/api/location").hasRole("PROTECTED")
                        // "/api/sos" 경로는 PROTECTED 역말만 접근 허용
                        .requestMatchers("/api/sos").hasRole("PROTECTED")
                        // 위에 명시되지 않은 나머지 모든 요청은 인증된 사용자만 접근 허용
                        .anyRequest().authenticated()
                )
                // Spring Security의 기본 인증 필터(UsernamePasswordAuthenticationFilter) 앞에
                // 우리가 직접 만든 JwtAuthenticationFilter를 추가하여 JWT 토큰 검사를 먼저 수행
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * CORS(Cross-Origin Resource Sharing) 정책을 설정하는 Bean.
     * 모든 출처(*), 모든 HTTP 메소드, 모든 헤더를 허용하도록 설정하여 개발 편의성을 높입니다.
     * (운영 환경에서는 보안을 위해 허용할 출처를 명시적으로 지정하는 것이 좋습니다.)
     *
     * @return CorsConfigurationSource 객체.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("*")); // 모든 출처 허용
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")); // 허용할 HTTP 메소드
        configuration.setAllowedHeaders(Arrays.asList("*")); // 모든 헤더 허용
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration); // 모든 경로에 대해 위 설정 적용
        return source;
    }
}