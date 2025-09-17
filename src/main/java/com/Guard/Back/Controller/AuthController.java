// AuthController.java
package com.Guard.Back.Controller;

import com.Guard.Back.Dto.AuthDto.*;
import com.Guard.Back.Service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController // 💡 이 어노테이션이 매우 중요합니다.
@RequestMapping("/api/auth") // 💡 이 주소가 정확한지 확인해주세요.
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup") // 💡 이 주소가 정확한지 확인해주세요.
    public ResponseEntity<Void> signUp(@RequestBody SignUpRequest request) {
        authService.signUp(request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/login")
    public ResponseEntity<Void> requestVerificationCode(@RequestBody LoginRequest request) {
        authService.sendVerificationCode(request.phoneNumber());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/verify")
    public ResponseEntity<AuthResponse> verifyCode(@RequestBody VerifyRequest request) {
        String token = authService.verifyCodeAndLogin(request.phoneNumber(), request.code());
        return ResponseEntity.ok(new AuthResponse(token));
    }
}