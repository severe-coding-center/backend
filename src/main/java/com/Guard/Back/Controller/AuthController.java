package com.Guard.Back.Controller;

import com.Guard.Back.Dto.AuthDto.*;
import com.Guard.Back.Service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

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