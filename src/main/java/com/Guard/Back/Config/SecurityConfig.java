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

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // 접근 권한 설정
                .authorizeHttpRequests(auth -> auth
                        // 1. 인증 없이 누구나 접근 가능한 경로
                        .requestMatchers(
                                "/api/auth/**",
                                "/api/protected/register",
                                "/oauth2/**",   // 로그인 시작 주소 허용
                                "/login/**",    // 기본 로그인 경로 허용
                                "/favicon.ico",
                                "/auth/callback"
                        ).permitAll()

                        .requestMatchers("/api/ocr/upload", "/api/tts").permitAll()

                        // GUARDIAN(보호자) 역할만 접근 가능한 경로
                        .requestMatchers(
                                "/api/users/me",                        // 내 정보 조회
                                "/api/users/fcm-token",                 // FCM 토큰 갱신
                                "/api/relationship/link",               // 관계 맺기
                                "/api/location/{protectedUserId}",      // 특정 피보호자 위치 조회
                                "/api/geofence/**",                     // 지오펜스 관련 모든 API
                                "/api/alerts/**"                        // 알림 기록 관련 모든 API
                        ).hasAnyRole("GUARDIAN", "ADMIN")


                        // PROTECTED(피보호자) 역할만 접근 가능한 경로
                        .requestMatchers(
                                "/api/location", // 위치 업로드 (POST)
                                "/api/sos"       // SOS 호출 (POST)
                        ).hasRole("PROTECTED")

                        .requestMatchers("/api/admin/**").hasRole("ADMIN")

                        // 위에 명시되지 않은 나머지 모든 요청은 인증만 되면 접근 가능
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // 요청이 들어오는 출처(Origin)를 명시
        configuration.setAllowedOrigins(Arrays.asList(
                // 프론트 개발 환경
                "http://192.168.0.38:5173",
                // 관리자 웹 주소 (리다이렉트 주소)
                "http://ceprj.gachon.ac.kr:60015",
                // 앱 서비스 주소
                "guard://callback"
        ));

        // 인증 정보(JWT)를 포함한 요청을 허용
        configuration.setAllowCredentials(true);

        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}