package com.Guard.Back.Auth;

import com.Guard.Back.Domain.RefreshToken;
import com.Guard.Back.Domain.User;
import com.Guard.Back.Dto.KakaoTokenResponseDto;
import com.Guard.Back.Dto.KakaoUserInfoResponseDto;
import com.Guard.Back.Jwt.JwtTokenProvider;
import com.Guard.Back.Repository.RefreshTokenRepository;
import com.Guard.Back.Repository.UserRepository;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.net.URLEncoder;
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

    @Value("${app.redirect-url}")
    private String appRedirectUrl;

    // ✅ GET으로 수정해야 카카오 리디렉션에 정상 대응 가능
    @GetMapping("/kakao-login")
    public void kakaoLogin(@RequestParam String code, HttpServletResponse response) throws IOException {

        // 1. 인가 코드를 이용해 Access Token 요청
        KakaoTokenResponseDto tokenResponse = webClient.post()
                .uri("https://kauth.kakao.com/oauth/token")
                .bodyValue("grant_type=authorization_code"
                        + "&client_id=" + clientId
                        + "&redirect_uri=" + redirectUri
                        + "&code=" + code)
                .retrieve()
                .bodyToMono(KakaoTokenResponseDto.class)
                .block();

        // 2. 카카오 사용자 정보 조회
        KakaoUserInfoResponseDto userInfo = webClient.get()
                .uri("https://kapi.kakao.com/v2/user/me")
                .header("Authorization", "Bearer " + tokenResponse.getAccess_token())
                .retrieve()
                .bodyToMono(KakaoUserInfoResponseDto.class)
                .block();

        String kakaoId = String.valueOf(userInfo.getId());
        String nickname = userInfo.getProperties().get("nickname");
        String profileImage = userInfo.getProperties().get("profile_image");

        // 3. 사용자 DB 저장 or 조회
        Optional<User> optionalUser = userRepository.findByKakaoId(kakaoId);
        User user = optionalUser.orElseGet(() ->
                userRepository.save(
                        User.builder()
                                .kakaoId(kakaoId)
                                .nickname(nickname)
                                .profileImage(profileImage)
                                .role("USER")
                                .build()
                )
        );

        // 4. JWT 발급
        String accessToken = jwtTokenProvider.createAccessToken(user.getId(), user.getNickname());
        String refreshToken = jwtTokenProvider.createRefreshToken();

        // 5. 기존 Refresh Token 제거 후 새로 저장
        refreshTokenRepository.findByUser(user).ifPresent(refreshTokenRepository::delete);
        RefreshToken newToken = RefreshToken.builder()
                .token(refreshToken)
                .user(user)
                .expiryDate(LocalDateTime.now().plusDays(7))
                .revoked(false)
                .build();
        refreshTokenRepository.save(newToken);

        // 6. React Native 앱으로 딥링크 리디렉션
        String redirectUrl = appRedirectUrl
                + "?accessToken=" + URLEncoder.encode(accessToken, "UTF-8")
                + "&refreshToken=" + URLEncoder.encode(refreshToken, "UTF-8")
                + "&nickname=" + URLEncoder.encode(user.getNickname(), "UTF-8");

        response.sendRedirect(redirectUrl);
    }
}
