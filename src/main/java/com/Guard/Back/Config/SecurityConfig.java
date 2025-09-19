package com.Guard.Back.Config;

import com.Guard.Back.Jwt.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * 애플리케이션의 보안 설정을 담당하는 클래스.
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    /**
     * HTTP 요청에 대한 보안 필터 체인을 설정합니다.
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // CSRF(Cross-Site Request Forgery) 공격 방어 기능을 비활성화합니다. (Stateless API 서버에서는 불필요)
                .csrf(AbstractHttpConfigurer::disable)
                // 세션을 사용하지 않는 Stateless 방식으로 서버를 운영하도록 설정합니다.
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // 각 HTTP 요청 경로에 대한 접근 권한을 설정합니다.
                .authorizeHttpRequests(auth -> auth
                        // 인증/회원가입 관련 API 경로는 누구나 접근할 수 있도록 허용합니다.
                        .requestMatchers("/api/auth/**", "/api/protected/**").permitAll()
                        // 그 외의 모든 요청은 반드시 인증(로그인)된 사용자만 접근할 수 있도록 설정합니다.
                        .anyRequest().authenticated()
                )
                // 우리가 직접 구현한 JwtAuthenticationFilter를 Spring Security의 기본 인증 필터 앞에 추가합니다.
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * 비밀번호 암호화를 위한 PasswordEncoder를 Spring Bean 으로 등록합니다.
     * BCrypt 해싱 알고리즘을 사용합니다.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}