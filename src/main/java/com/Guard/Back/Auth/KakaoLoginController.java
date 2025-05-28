// KakaoLoginController.java - 카카오 인가 코드를 받아 JWT를 발급하고 RefreshToken을 저장
package com.Guard.Back.Auth;

import com.Guard.Back.Domain.RefreshToken;
import com.Guard.Back.Domain.User;
import com.Guard.Back.Dto.KakaoTokenResponseDto;
import com.Guard.Back.Dto.KakaoUserInfoResponseDto;
import com.Guard.Back.Jwt.JwtTokenProvider;
import com.Guard.Back.Repository.RefreshTokenRepository;
import com.Guard.Back.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class KakaoLoginController {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final WebClient webClient;

    @Value("${kakao.client-id}")
    private String clientId;

    @Value("${kakao.redirect-uri}")
    private String redirectUri;

    // 카카오 인가 코드를 이용해 로그인 처리 + JWT 발급 + RefreshToken 저장
    @PostMapping("/kakao-login")
    public ResponseEntity<?> kakaoLogin(@RequestParam String code) {

        // 1. 카카오 토큰 요청
        KakaoTokenResponseDto tokenResponse = webClient.post()
                .uri("https://kauth.kakao.com/oauth/token")
                .bodyValue("grant_type=authorization_code"
                        + "&client_id=" + clientId
                        + "&redirect_uri=" + redirectUri
                        + "&code=" + code)
                .retrieve()
                .bodyToMono(KakaoTokenResponseDto.class)
                .block();

        // 2. 사용자 정보 요청
        KakaoUserInfoResponseDto userInfo = webClient.get()
                .uri("https://kapi.kakao.com/v2/user/me")
                .header("Authorization", "Bearer " + tokenResponse.getAccess_token())
                .retrieve()
                .bodyToMono(KakaoUserInfoResponseDto.class)
                .block();

        String kakaoId = String.valueOf(userInfo.getId());
        String nickname = userInfo.getProperties().get("nickname");
        String profileImage = userInfo.getProperties().get("profile_image");

        // 3. DB에 사용자 저장 or 조회
        Optional<User> optionalUser = userRepository.findByKakaoId(kakaoId);
        User user = optionalUser.orElseGet(() -> userRepository.save(
                User.builder()
                        .kakaoId(kakaoId)
                        .nickname(nickname)
                        .profileImage(profileImage)
                        .role("USER")
                        .build()
        ));

        // 4. JWT 토큰 발급
        String accessToken = jwtTokenProvider.createAccessToken(user.getId(), user.getNickname());
        String refreshToken = jwtTokenProvider.createRefreshToken();

        // 5. RefreshToken 저장 또는 갱신
        refreshTokenRepository.findByUser(user).ifPresent(refreshTokenRepository::delete);
        RefreshToken newToken = RefreshToken.builder()
                .token(refreshToken)
                .user(user)
                .expiryDate(LocalDateTime.now().plusDays(7))
                .revoked(false)
                .build();
        refreshTokenRepository.save(newToken);

        // 6. 클라이언트에 응답
        return ResponseEntity.ok().body(
                new LoginResponseDto(accessToken, refreshToken, user.getNickname())
        );
    }

    // 내부 응답 DTO
    record LoginResponseDto(String accessToken, String refreshToken, String nickname) {}
}
