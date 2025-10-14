package com.Guard.Back.Config;

import com.Guard.Back.Jwt.JwtAuthenticationFilter; // 💡 필터 임포트
import lombok.RequiredArgsConstructor; // 💡 RequiredArgsConstructor 임포트
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter; // 💡 임포트
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@RequiredArgsConstructor // 💡 final 필드 주입을 위해 추가
public class SecurityConfig {

    // 💡 [추가] JWT 인증 필터를 주입받습니다.
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // 인증 없이 접근 가능한 경로
                        .requestMatchers("/api/auth/**", "/api/protected/register").permitAll()

                        // GUARDIAN 역할만 접근 가능한 경로
                        .requestMatchers("/api/relationship/link", "/api/location/{protectedUserId}").hasRole("GUARDIAN")

                        // PROTECTED 역할만 접근 가능한 경로
                        .requestMatchers("/api/location").hasRole("PROTECTED")

                        // 그 외 모든 요청은 인증만 되면 접근 가능
                        .anyRequest().authenticated()
                )
                // 💡 [핵심 추가] UsernamePasswordAuthenticationFilter 앞에 우리가 만든 JWT 필터를 추가합니다.
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}