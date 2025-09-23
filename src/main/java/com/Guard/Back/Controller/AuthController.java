package com.Guard.Back.Controller;

import com.Guard.Back.Domain.User;
import com.Guard.Back.Dto.AuthDto.*;
import com.Guard.Back.Jwt.JwtTokenProvider;
import com.Guard.Back.Service.AuthService;
import com.Guard.Back.Service.TokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * 보호자(User)의 인증(회원가입, 로그인) 관련 API 요청을 처리하는 컨트롤러.
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final JwtTokenProvider jwtTokenProvider;
    private final TokenService tokenService;

    /**
     * 보호자 회원가입 API.
     * @param request 회원가입 정보(이름, 전화번호)
     * @return 생성된 초기 비밀번호를 포함한 성공 메시지
     */
    @PostMapping("/signup")
    public ResponseEntity<String> signUp(@RequestBody SignUpRequest request) {
        String initialPassword = authService.signUp(request);
        return ResponseEntity.ok("회원가입 성공! 초기 비밀번호: " + initialPassword);
    }

    /**
     * 보호자 로그인 API.
     * @param request 로그인 정보(전화번호, 비밀번호)
     * @return AccessToken과 RefreshToken을 담은 응답
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        // 1. AuthService를 통해 사용자 인증을 수행하고, 인증된 User 객체를 받습니다.
        User user = authService.login(request);

        // 2. JwtTokenProvider를 통해 토큰들을 생성합니다.
        String accessToken = jwtTokenProvider.createAccessToken(user.getId(), "GUARDIAN");
        String refreshToken = jwtTokenProvider.createRefreshToken();

        // 3. TokenService를 통해 RefreshToken을 DB에 저장/갱신합니다.
        tokenService.saveOrUpdateRefreshToken(user, null, refreshToken);

        // 4. 생성된 토큰들을 클라이언트에게 반환합니다.
        return ResponseEntity.ok(new AuthResponse(accessToken, refreshToken));
    }
    /**
     * Access Token 재발급 API.
     * @param request Refresh Token을 담은 요청
     * @return 새로 발급된 Access Token
     */
    @PostMapping("/refresh")
    public ResponseEntity<RefreshResponse> refresh(@RequestBody RefreshRequest request) {
        String newAccessToken = tokenService.reissueAccessToken(request.refreshToken());
        return ResponseEntity.ok(new RefreshResponse(newAccessToken));
    }

    /**
     * 로그아웃 API.
     * @param authentication 현재 로그인한 사용자의 정보
     * @return 성공 메시지
     */
    @PostMapping("/logout")
    public ResponseEntity<String> logout(Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        String userType = (String) authentication.getCredentials();
        tokenService.logout(userId, userType);
        return ResponseEntity.ok("로그아웃 되었습니다.");
    }

}