package com.Guard.Back.Controller;

import com.Guard.Back.Domain.User;
import com.Guard.Back.Dto.AuthDto.*;
import com.Guard.Back.Jwt.JwtTokenProvider;
import com.Guard.Back.Service.KakaoOAuthService;
import com.Guard.Back.Service.TokenService;
import com.Guard.Back.Repository.UserRepository;
import com.Guard.Back.Domain.OAuthProvider;
import com.Guard.Back.Dto.OAuthUserInfoDto;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication; // 💡 Authentication 임포트
import org.springframework.web.bind.annotation.*;
import java.io.IOException;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final KakaoOAuthService kakaoOAuthService;
    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final TokenService tokenService;

    @Value("${kakao.client-id}")
    private String kakaoClientId;

    @Value("${kakao.redirect-uri}")
    private String kakaoRedirectUri;

    @GetMapping("/login/kakao")
    public void redirectToKakaoLogin(HttpServletResponse response) throws IOException {
        String kakaoAuthUrl = "https://kauth.kakao.com/oauth/authorize?client_id="
                + kakaoClientId + "&redirect_uri=" + kakaoRedirectUri + "&response_type=code";
        response.sendRedirect(kakaoAuthUrl);
    }

    @GetMapping("/login/kakao/callback")
    public ResponseEntity<AuthResponse> kakaoLoginCallback(@RequestParam("code") String code) {
        OAuthUserInfoDto userInfo = kakaoOAuthService.getUserInfo(code);

        User user = userRepository.findByProviderAndProviderId(OAuthProvider.KAKAO, userInfo.getProviderId())
                .orElseGet(() -> {
                    User newUser = User.builder()
                            .provider(OAuthProvider.KAKAO)
                            .providerId(userInfo.getProviderId())
                            .nickname(userInfo.getNickname())
                            .profileImage(userInfo.getProfileImage())
                            .build();
                    return userRepository.save(newUser);
                });

        String accessToken = jwtTokenProvider.createAccessToken(user.getId(), "GUARDIAN");
        String refreshToken = jwtTokenProvider.createRefreshToken();
        tokenService.saveOrUpdateRefreshToken(user, null, refreshToken);

        return ResponseEntity.ok(new AuthResponse(accessToken, refreshToken));
    }

    /**
     * 💡 [추가] Access Token 및 Refresh Token 재발급 API.
     */
    @PostMapping("/refresh")
    public ResponseEntity<RefreshResponse> refresh(@RequestBody RefreshRequest request) {
        RefreshResponse newTokens = tokenService.reissueTokens(request.refreshToken());
        return ResponseEntity.ok(newTokens);
    }

    /**
     * 💡 [추가] 로그아웃 API.
     */
    @PostMapping("/logout")
    public ResponseEntity<String> logout(Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        String userType = (String) authentication.getCredentials();
        tokenService.logout(userId, userType);
        return ResponseEntity.ok("로그아웃 되었습니다.");
    }
}