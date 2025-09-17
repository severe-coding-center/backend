package com.Guard.Back.Config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // stateless REST API 이므로 csrf, formLogin, httpBasic 비활성화
                .csrf(csrf -> csrf.disable())
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable())

                // 세션을 사용하지 않음
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // URL 경로별 접근 권한 설정
                .authorizeHttpRequests(auth -> auth
                        // "/api/auth/" 로 시작하는 모든 경로는 인증 없이 접근 허용
                        .requestMatchers("/api/auth/**").permitAll()
                        // 그 외의 모든 요청은 반드시 인증을 거쳐야 함
                        .anyRequest().authenticated()
                );

        // TODO: 나중에 JWT 토큰을 검증하는 필터를 여기에 추가해야 합니다.

        return http.build();
    }
}