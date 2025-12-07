package com.Guard.Back.Controller;

import com.Guard.Back.Domain.OAuthProvider;
import com.Guard.Back.Domain.User;
import com.Guard.Back.Domain.UserRole;
import com.Guard.Back.Dto.AuthDto.*;
import com.Guard.Back.Dto.OAuthUserInfoDto;
import com.Guard.Back.Jwt.JwtTokenProvider;
import com.Guard.Back.Repository.UserRepository;
import com.Guard.Back.Service.GoogleOAuthService;
import com.Guard.Back.Service.KakaoOAuthService;
import com.Guard.Back.Service.TokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value; // 👈 [수정] 이 import가 없어서 에러가 났었습니다!
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final KakaoOAuthService kakaoOAuthService;
    private final GoogleOAuthService googleOAuthService;
    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final TokenService tokenService;

    // 💡 [수정] 필드는 클래스 상단에 모아두는 것이 관례입니다.
    @Value("${admin.web.url}")
    private String adminWebUrl;

    /**
     * 카카오 로그인 (앱 사용자용)
     */
    @GetMapping("/login/kakao/callback")
    public RedirectView kakaoLoginCallback(@RequestParam("code") String code) {
        log.info("[카카오 로그인] 인증 시작");
        OAuthUserInfoDto userInfo = kakaoOAuthService.getUserInfo(code);

        User user = userRepository.findByProviderAndProviderId(OAuthProvider.KAKAO, userInfo.getProviderId())
                .orElseGet(() -> {
                    log.info("[카카오 로그인] 신규 회원 가입 진행");
                    return userRepository.save(User.builder()
                            .provider(OAuthProvider.KAKAO)
                            .providerId(userInfo.getProviderId())
                            .nickname(userInfo.getNickname())
                            .profileImage(userInfo.getProfileImage())
                            .role(UserRole.GUARDIAN) // 👈 [중요] 카카오 유저도 기본 권한 설정
                            .build());
                });

        String accessToken = jwtTokenProvider.createAccessToken(user.getId(), UserRole.GUARDIAN);
        String refreshToken = jwtTokenProvider.createRefreshToken();
        tokenService.saveOrUpdateRefreshToken(user, null, refreshToken);

        // 앱 딥링크로 리다이렉트
        String deepLinkUrl = UriComponentsBuilder.fromUriString("guard://callback")
                .queryParam("accessToken", accessToken)
                .queryParam("refreshToken", refreshToken)
                .queryParam("nickname", user.getNickname())
                .queryParam("kakaoId", user.getProviderId())
                .build().encode().toUriString();

        return new RedirectView(deepLinkUrl);
    }

    /**
     * 구글 로그인 (관리자 웹용)
     */
    @GetMapping("/login/google/callback")
    public RedirectView googleLoginCallback(@RequestParam("code") String code) {
        log.info("[관리자 로그인] 구글 인증 시도");
        OAuthUserInfoDto userInfo = googleOAuthService.getUserInfo(code);

        User user = userRepository.findByProviderAndProviderId(OAuthProvider.GOOGLE, userInfo.getProviderId())
                .orElseGet(() -> userRepository.save(User.builder()
                        .provider(OAuthProvider.GOOGLE)
                        .providerId(userInfo.getProviderId())
                        .nickname(userInfo.getNickname())
                        .email(userInfo.getEmail())
                        .profileImage(userInfo.getProfileImage())
                        .role(UserRole.GUARDIAN) // 초기 생성 시엔 관리자 권한 없음
                        .build()));

        // 🚨 DB에 'ADMIN' 권한이 있는지 확인
        if (user.getRole() != UserRole.ADMIN) {
            log.warn("[로그인 실패] 권한 없는 관리자 접근: {}", userInfo.getEmail());
            return new RedirectView(adminWebUrl + "?error=unauthorized");
        }

        // 관리자 토큰 발급
        String accessToken = jwtTokenProvider.createAccessToken(user.getId(), UserRole.ADMIN);
        String refreshToken = jwtTokenProvider.createRefreshToken();
        tokenService.saveOrUpdateRefreshToken(user, null, refreshToken);

        // 웹 프론트엔드로 리다이렉트
        String redirectUrl = UriComponentsBuilder.fromUriString(adminWebUrl)
                .queryParam("accessToken", accessToken)
                .queryParam("refreshToken", refreshToken)
                .build().toUriString();

        return new RedirectView(redirectUrl);
    }

    /**
     * 토큰 재발급
     */
    @PostMapping("/refresh")
    public ResponseEntity<RefreshResponse> refresh(@RequestBody RefreshRequest request) {
        RefreshResponse newTokens = tokenService.reissueTokens(request.refreshToken());
        return ResponseEntity.ok(newTokens);
    }

    /**
     * 로그아웃
     */
    @PostMapping("/logout")
    public ResponseEntity<String> logout(Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        String role = authentication.getAuthorities().stream()
                .findFirst()
                .map(GrantedAuthority::getAuthority)
                .orElse(null);

        tokenService.logout(userId, role);
        return ResponseEntity.ok("로그아웃 되었습니다.");
    }
}