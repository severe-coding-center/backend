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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j; // 💡 Slf4j 임포트 추가
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;
import java.util.Collection;

/**
 * 사용자 인증(로그인, 로그아웃, 토큰 재발급) 관련 API 요청을 처리하는 컨트롤러.
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j // 💡 로깅을 위한 어노테이션 추가
public class AuthController {

    private final KakaoOAuthService kakaoOAuthService;
    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final TokenService tokenService;

    /**
     * 카카오 로그인 콜백을 처리하여 사용자를 로그인/회원가입 시키고 JWT 토큰을 발급합니다.
     *
     * @param code 카카오 서버로부터 받은 인가 코드.
     * @return 성공 시 Access Token과 Refresh Token이 담긴 DTO.
     */
    @GetMapping("/login/kakao/callback")
    public ResponseEntity<AuthResponse> kakaoLoginCallback(@RequestParam("code") String code) {
        log.info("[카카오 로그인] 인가 코드를 이용한 로그인/회원가입을 시작합니다.");
        OAuthUserInfoDto userInfo = kakaoOAuthService.getUserInfo(code);

        User user = userRepository.findByProviderAndProviderId(OAuthProvider.KAKAO, userInfo.getProviderId())
                .orElseGet(() -> {
                    log.info("[카카오 로그인] 새로운 사용자(providerId: {})를 회원가입 시킵니다.", userInfo.getProviderId());
                    User newUser = User.builder()
                            .provider(OAuthProvider.KAKAO)
                            .providerId(userInfo.getProviderId())
                            .nickname(userInfo.getNickname())
                            .profileImage(userInfo.getProfileImage())
                            .build();
                    return userRepository.save(newUser);
                });

        log.info("[카카오 로그인] 사용자 ID: {}에 대한 JWT 토큰을 발급합니다.", user.getId());
        String accessToken = jwtTokenProvider.createAccessToken(user.getId(), UserRole.GUARDIAN);
        String refreshToken = jwtTokenProvider.createRefreshToken();
        tokenService.saveOrUpdateRefreshToken(user, null, refreshToken);

        log.info("[카카오 로그인] 사용자 ID: {}의 로그인이 성공적으로 완료되었습니다.", user.getId());
        return ResponseEntity.ok(new AuthResponse(accessToken, refreshToken));
    }

    /**
     * 유효한 Refresh Token을 사용하여 새로운 Access Token과 Refresh Token을 재발급합니다.
     *
     * @param request 재발급을 요청하는 Refresh Token을 담은 DTO.
     * @return 새로 발급된 Access Token과 Refresh Token이 담긴 DTO.
     */
    @PostMapping("/refresh")
    public ResponseEntity<RefreshResponse> refresh(@RequestBody RefreshRequest request) {
        log.info("[토큰 재발급] Refresh Token을 이용한 토큰 재발급을 시작합니다.");
        RefreshResponse newTokens = tokenService.reissueTokens(request.refreshToken());
        log.info("[토큰 재발급] 토큰 재발급이 성공적으로 완료되었습니다.");
        return ResponseEntity.ok(newTokens);
    }

    /**
     * 현재 로그인된 사용자를 로그아웃 처리합니다.
     * 서버에 저장된 Refresh Token을 삭제합니다.
     *
     * @param authentication 현재 로그인한 사용자의 인증 정보.
     * @return 성공 메시지.
     */
    @PostMapping("/logout")
    public ResponseEntity<String> logout(Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        String role = authentication.getAuthorities().stream()
                .findFirst()
                .map(GrantedAuthority::getAuthority)
                .orElse(null);

        log.info("[로그아웃] 사용자 ID: {}, 역할: {}의 로그아웃을 시작합니다.", userId, role);
        tokenService.logout(userId, role);
        log.info("[로그아웃] 사용자 ID: {}의 로그아웃이 성공적으로 완료되었습니다.", userId);
        return ResponseEntity.ok("로그아웃 되었습니다.");
    }
}