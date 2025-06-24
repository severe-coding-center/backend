package com.Guard.Back.Auth;

import com.Guard.Back.Domain.*;
import com.Guard.Back.Dto.OAuthUserInfoDto;
import com.Guard.Back.Jwt.JwtTokenProvider;
import com.Guard.Back.Repository.*;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class OAuthController {

    private final List<OAuthService> oAuthServices;           // 모든 구현체 자동 주입
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtTokenProvider jwtTokenProvider;

    @Value("${app.redirect-url}")
    private String appRedirectUrl;

    /**
     * /api/auth/login/{provider}?code=...&state=...
     */
    @PostMapping("/login/{provider}")
    public void login(@PathVariable("provider") OAuthProvider provider,
                      @RequestParam String code,
                      @RequestParam(required = false) String state,
                      HttpServletResponse response) throws Exception {

        // 1) provider 에 맞는 서비스 선택
        OAuthService service = oAuthServices.stream()
                .filter(s -> s.provider() == provider)
                .findFirst().orElseThrow(() -> new IllegalArgumentException("Unsupported provider"));

        // 2) 사용자 정보 획득
        OAuthUserInfoDto info = service.getUserInfo(code, state);

        // 3) DB 조회/등록
        User user = userRepository.findByProviderAndProviderId(provider, info.getId())
                .orElseGet(() -> userRepository.save(
                        User.builder()
                                .provider(provider)
                                .providerId(info.getId())
                                .nickname(Optional.ofNullable(info.getNickname()).orElse("사용자"))
                                .email(info.getEmail())
                                .profileImage(info.getProfileImage())
                                .build()
                ));

        // 4) JWT 발급
        String accessToken  = jwtTokenProvider.createAccessToken(user.getId(), user.getNickname());
        String refreshToken = jwtTokenProvider.createRefreshToken();

        refreshTokenRepository.findByUser(user).ifPresent(refreshTokenRepository::delete);
        refreshTokenRepository.save(RefreshToken.builder()
                .token(refreshToken)
                .user(user)
                .expiryDate(LocalDateTime.now().plusDays(7))
                .build());

        // 5) 앱 딥링크 리다이렉션
        String redirectUrl = appRedirectUrl +
                "?accessToken="  + URLEncoder.encode(accessToken, StandardCharsets.UTF_8) +
                "&refreshToken=" + URLEncoder.encode(refreshToken, StandardCharsets.UTF_8) +
                "&nickname="     + URLEncoder.encode(user.getNickname(), StandardCharsets.UTF_8);

        response.sendRedirect(redirectUrl);
    }
}