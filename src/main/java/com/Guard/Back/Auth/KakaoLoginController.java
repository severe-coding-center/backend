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
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
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

    @PostMapping("/kakao-login")
    public void kakaoLogin(@RequestParam String code, HttpServletResponse response) throws IOException {
        // ✅ 1. 카카오 토큰 요청 (form 방식으로)
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("grant_type", "authorization_code");
        formData.add("client_id", clientId);
        formData.add("redirect_uri", redirectUri);
        formData.add("code", code);

        KakaoTokenResponseDto tokenResponse = webClient.post()
                .uri("https://kauth.kakao.com/oauth/token")
                .header("Content-Type", "application/x-www-form-urlencoded")
                .body(BodyInserters.fromFormData(formData))
                .retrieve()
                .bodyToMono(KakaoTokenResponseDto.class)
                .block();

        // ✅ 2. 사용자 정보 요청
        KakaoUserInfoResponseDto userInfo = webClient.get()
                .uri("https://kapi.kakao.com/v2/user/me")
                .header("Authorization", "Bearer " + tokenResponse.getAccess_token())
                .retrieve()
                .bodyToMono(KakaoUserInfoResponseDto.class)
                .block();

        String kakaoId = String.valueOf(userInfo.getId());
        String nickname = userInfo.getProperties().get("nickname");
        String profileImage = userInfo.getProperties().get("profile_image");

        // ✅ 3. 사용자 DB 등록 or 조회
        Optional<User> optionalUser = userRepository.findByKakaoId(kakaoId);
        User user = optionalUser.orElseGet(() -> userRepository.save(
                User.builder()
                        .kakaoId(kakaoId)
                        .nickname(nickname)
                        .profileImage(profileImage)
                        .role("USER")
                        .build()
        ));

        // ✅ 4. 토큰 생성
        String accessToken = jwtTokenProvider.createAccessToken(user.getId(), user.getNickname());
        String refreshToken = jwtTokenProvider.createRefreshToken();

        // ✅ 5. RefreshToken 저장 (기존 토큰 제거)
        refreshTokenRepository.findByUser(user).ifPresent(refreshTokenRepository::delete);
        RefreshToken newToken = RefreshToken.builder()
                .token(refreshToken)
                .user(user)
                .expiryDate(LocalDateTime.now().plusDays(7))
                .revoked(false)
                .build();
        refreshTokenRepository.save(newToken);

        // ✅ 6. 앱으로 리디렉션 (딥링크 guard://...)
        String redirectUrl = appRedirectUrl
                + "?accessToken=" + URLEncoder.encode(accessToken, StandardCharsets.UTF_8)
                + "&refreshToken=" + URLEncoder.encode(refreshToken, StandardCharsets.UTF_8)
                + "&nickname=" + URLEncoder.encode(user.getNickname(), StandardCharsets.UTF_8);

        response.sendRedirect(redirectUrl);
    }

    // ⚠️ 딥링크용 응답 DTO는 필요 없지만 참고용으로 남겨둠
    record LoginResponseDto(String accessToken, String refreshToken, String nickname) {}
}
