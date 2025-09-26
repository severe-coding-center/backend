package com.Guard.Back.Controller;

import com.Guard.Back.Domain.User;
import com.Guard.Back.Dto.AuthDto.*;
import com.Guard.Back.Jwt.JwtTokenProvider;
import com.Guard.Back.Service.KakaoOAuthService;
import com.Guard.Back.Service.TokenService;
import com.Guard.Back.Repository.UserRepository;
import com.Guard.Back.Domain.OAuthProvider;
import com.Guard.Back.Dto.OAuthUserInfoDto;
import jakarta.servlet.http.HttpServletResponse; // response 임포트
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value; // Value 임포트
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.io.IOException; // IOException 임포트

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

    // 💡 [신규] 클라이언트(앱)가 카카오 로그인 창을 띄우기 위해 호출하는 API
    @GetMapping("/login/kakao")
    public void redirectToKakaoLogin(HttpServletResponse response) throws IOException {
        String kakaoAuthUrl = "https://kauth.kakao.com/oauth/authorize?client_id="
                + kakaoClientId + "&redirect_uri=" + kakaoRedirectUri + "&response_type=code";
        response.sendRedirect(kakaoAuthUrl);
    }

    /**
     * 💡 [변경] 카카오 로그인 성공 후 Redirect 되는 API (서버 전용)
     * @param code 카카오로부터 받은 인증 코드
     * @return 우리 서비스의 Access Token과 Refresh Token
     */
    @GetMapping("/login/kakao/callback")
    public ResponseEntity<AuthResponse> kakaoLoginCallback(@RequestParam("code") String code) {
        // 1. 받은 코드로 카카오 사용자 정보 요청
        OAuthUserInfoDto userInfo = kakaoOAuthService.getUserInfo(code);

        // 2. 사용자 조회 또는 신규 저장
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

        // 3. 우리 서비스의 JWT 발급 및 저장
        String accessToken = jwtTokenProvider.createAccessToken(user.getId(), "GUARDIAN");
        String refreshToken = jwtTokenProvider.createRefreshToken();
        tokenService.saveOrUpdateRefreshToken(user, null, refreshToken);

        // 4. 클라이언트에게 토큰 반환
        return ResponseEntity.ok(new AuthResponse(accessToken, refreshToken));
    }

    // Refresh, Logout API는 기존과 동일하게 유지
}