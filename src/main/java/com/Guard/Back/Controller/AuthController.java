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

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final JwtTokenProvider jwtTokenProvider;
    private final TokenService tokenService;

    @PostMapping("/signup/send-code")
    public ResponseEntity<String> sendSignUpCode(@RequestBody PhoneRequest request) {
        authService.sendVerificationCode(request);
        return ResponseEntity.ok("인증번호가 발송되었습니다.");
    }

    @PostMapping("/signup/verify-code")
    public ResponseEntity<String> verifySignUpCode(@RequestBody VerificationRequest request) {
        boolean isVerified = authService.verifyCode(request);
        if (isVerified) {
            return ResponseEntity.ok("인증에 성공했습니다.");
        } else {
            return ResponseEntity.badRequest().body("인증번호가 올바르지 않습니다.");
        }
    }

    @PostMapping("/signup")
    public ResponseEntity<String> signUp(@RequestBody SignUpRequest request) {
        authService.signUp(request);
        return ResponseEntity.ok("회원가입이 완료되었습니다.");
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        User user = authService.login(request);
        String accessToken = jwtTokenProvider.createAccessToken(user.getId(), "GUARDIAN");
        String refreshToken = jwtTokenProvider.createRefreshToken();
        tokenService.saveOrUpdateRefreshToken(user, null, refreshToken);
        return ResponseEntity.ok(new AuthResponse(accessToken, refreshToken));
    }

    /**
     * 💡 [수정] Access Token 및 Refresh Token 재발급 API.
     * @param request Refresh Token을 담은 요청
     * @return 새로 발급된 Access Token과 Refresh Token
     */
    @PostMapping("/refresh")
    public ResponseEntity<RefreshResponse> refresh(@RequestBody RefreshRequest request) {
        RefreshResponse newTokens = tokenService.reissueTokens(request.refreshToken());
        return ResponseEntity.ok(newTokens);
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        String userType = (String) authentication.getCredentials();
        tokenService.logout(userId, userType);
        return ResponseEntity.ok("로그아웃 되었습니다.");
    }
}