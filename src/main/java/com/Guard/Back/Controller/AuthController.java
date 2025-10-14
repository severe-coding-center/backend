package com.Guard.Back.Controller;

import com.Guard.Back.Domain.User;
import com.Guard.Back.Domain.UserRole; // 💡 import 추가
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
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.io.IOException;
import java.util.Collection;
import org.springframework.security.core.GrantedAuthority;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final KakaoOAuthService kakaoOAuthService;
    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final TokenService tokenService;

    // ... (카카오 로그인 관련 부분은 기존과 동일) ...
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

        // 💡 [수정] "GUARDIAN" 문자열 대신 UserRole.GUARDIAN Enum을 사용합니다.
        String accessToken = jwtTokenProvider.createAccessToken(user.getId(), UserRole.GUARDIAN);
        String refreshToken = jwtTokenProvider.createRefreshToken();
        tokenService.saveOrUpdateRefreshToken(user, null, refreshToken);

        return ResponseEntity.ok(new AuthResponse(accessToken, refreshToken));
    }


    @PostMapping("/refresh")
    public ResponseEntity<RefreshResponse> refresh(@RequestBody RefreshRequest request) {
        RefreshResponse newTokens = tokenService.reissueTokens(request.refreshToken());
        return ResponseEntity.ok(newTokens);
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());

        // 💡 [수정] getCredentials() 대신 getAuthorities()를 사용하여 역할을 확인합니다.
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        String role = authorities.stream()
                .findFirst()
                .map(GrantedAuthority::getAuthority)
                .orElse(null);

        tokenService.logout(userId, role);
        return ResponseEntity.ok("로그아웃 되었습니다.");
    }
}