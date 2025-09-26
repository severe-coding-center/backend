package com.Guard.Back.Controller;

import com.Guard.Back.Domain.User;
import com.Guard.Back.Dto.AuthDto.*;
import com.Guard.Back.Jwt.JwtTokenProvider;
import com.Guard.Back.Service.KakaoOAuthService;
import com.Guard.Back.Service.TokenService;
import com.Guard.Back.Repository.UserRepository;
import com.Guard.Back.Domain.OAuthProvider;
import com.Guard.Back.Dto.OAuthUserInfoDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final KakaoOAuthService kakaoOAuthService;
    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final TokenService tokenService;

    /**
     * 💡 [신규] 카카오 로그인 API
     * @param kakaoAccessToken 카카오로부터 받은 Access Token
     * @return 우리 서비스의 Access Token과 Refresh Token
     */
    @PostMapping("/login/kakao")
    public ResponseEntity<AuthResponse> kakaoLogin(@RequestHeader("Authorization") String kakaoAccessToken) {
        // 1. 카카오 사용자 정보 요청
        OAuthUserInfoDto userInfo = kakaoOAuthService.getUserInfo(kakaoAccessToken);

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

        // 3. 우리 서비스의 JWT 발급
        String accessToken = jwtTokenProvider.createAccessToken(user.getId(), "GUARDIAN");
        String refreshToken = jwtTokenProvider.createRefreshToken();

        // 4. Refresh Token 저장/갱신
        tokenService.saveOrUpdateRefreshToken(user, null, refreshToken);

        return ResponseEntity.ok(new AuthResponse(accessToken, refreshToken));
    }

    // Refresh, Logout API는 기존과 동일하게 유지 (코드는 생략)
}