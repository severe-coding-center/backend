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
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView; // 💡 딥링크를 위한 import
import org.springframework.web.util.UriComponentsBuilder;  // 💡 딥링크를 위한 import
import java.util.Collection;

/**
 * 사용자 인증(로그인, 로그아웃, 토큰 재발급) 관련 API 요청을 처리하는 컨트롤러.
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final KakaoOAuthService kakaoOAuthService;
    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final TokenService tokenService;

    /**
     * 카카오 로그인 콜백을 처리하고, JWT 토큰을 담은 딥링크로 리디렉션합니다.
     * 이 방식은 웹 브라우저에서 로그인 성공 후, 자동으로 앱을 실행시켜 토큰을 전달하는 사용자 친화적인 방식입니다.
     *
     * @param code 카카오 서버로부터 받은 인가 코드.
     * @return Access Token, Refresh Token 등을 쿼리 파라미터로 포함하는 딥링크 RedirectView 객체.
     */
    @GetMapping("/login/kakao/callback")
    public RedirectView kakaoLoginCallback(@RequestParam("code") String code) {
        log.info("[카카오 딥링크 로그인] 인가 코드를 이용한 로그인/회원가입을 시작합니다.");
        OAuthUserInfoDto userInfo = kakaoOAuthService.getUserInfo(code);

        User user = userRepository.findByProviderAndProviderId(OAuthProvider.KAKAO, userInfo.getProviderId())
                .orElseGet(() -> {
                    log.info("[카카오 딥링크 로그인] 새로운 사용자(providerId: {})를 회원가입 시킵니다.", userInfo.getProviderId());
                    User newUser = User.builder()
                            .provider(OAuthProvider.KAKAO)
                            .providerId(userInfo.getProviderId())
                            .nickname(userInfo.getNickname())
                            .profileImage(userInfo.getProfileImage())
                            .build();
                    return userRepository.save(newUser);
                });

        log.info("[카카오 딥링크 로그인] 사용자 ID: {}에 대한 JWT 토큰을 발급합니다.", user.getId());
        String accessToken = jwtTokenProvider.createAccessToken(user.getId(), UserRole.GUARDIAN);
        String refreshToken = jwtTokenProvider.createRefreshToken();
        tokenService.saveOrUpdateRefreshToken(user, null, refreshToken);

        // 앱으로 리디렉션할 딥링크 주소를 생성합니다.
        String deepLinkUrl = UriComponentsBuilder.fromUriString("guard://callback") // 앱과 약속된 스킴(Scheme)
                .queryParam("accessToken", accessToken)
                .queryParam("refreshToken", refreshToken)
                .queryParam("nickname", user.getNickname())
                .queryParam("kakaoId", user.getProviderId())
                .build()
                .encode() // URL에 포함될 수 없는 문자(한글, 특수문자 등)를 안전하게 인코딩
                .toUriString();

        log.info("[카카오 딥링크 로그인] 사용자 ID: {}를 위한 딥링크를 생성하여 리디렉션합니다.", user.getId());
        return new RedirectView(deepLinkUrl);
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