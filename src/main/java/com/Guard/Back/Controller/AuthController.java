package com.Guard.Back.Controller;

import com.Guard.Back.Domain.User;
import com.Guard.Back.Domain.UserRole;
import com.Guard.Back.Dto.AuthDto.*;
import com.Guard.Back.Jwt.JwtTokenProvider;
import com.Guard.Back.Service.KakaoOAuthService;
import com.Guard.Back.Service.TokenService;
import com.Guard.Back.Repository.UserRepository;
import com.Guard.Back.Domain.OAuthProvider;
import com.Guard.Back.Dto.OAuthUserInfoDto;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Collection;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final KakaoOAuthService kakaoOAuthService;
    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final TokenService tokenService;

    // kakaoRedirectUri, kakaoClientId는 application-API-KEY.properties에서 주입됩니다.
    // @Value 어노테이션은 더 이상 필요 없습니다.

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

        String accessToken = jwtTokenProvider.createAccessToken(user.getId(), UserRole.GUARDIAN);
        String refreshToken = jwtTokenProvider.createRefreshToken();

        // 💡 [수정] TokenService의 메소드 이름이 saveOrUpdateRefreshToken 입니다.
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

        String role = authentication.getAuthorities().stream()
                .findFirst()
                .map(GrantedAuthority::getAuthority)
                .orElse(null);

        // 💡 [수정] 변경된 메소드 시그니처에 맞게 호출합니다.
        tokenService.logout(userId, role);
        return ResponseEntity.ok("로그아웃 되었습니다.");
    }
}