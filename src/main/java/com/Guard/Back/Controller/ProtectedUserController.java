package com.Guard.Back.Controller;

import com.Guard.Back.Domain.ProtectedUser;
import com.Guard.Back.Dto.RegisterRequest;   // 💡 import 변경
import com.Guard.Back.Dto.RegisterResponse;  // 💡 import 변경
import com.Guard.Back.Jwt.JwtTokenProvider;
import com.Guard.Back.Service.ProtectedUserService;
import com.Guard.Back.Service.TokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/protected")
@RequiredArgsConstructor
public class ProtectedUserController {

    private final ProtectedUserService protectedUserService;
    private final JwtTokenProvider jwtTokenProvider;
    private final TokenService tokenService;

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@RequestBody RegisterRequest request) { // 💡 타입 변경
        ProtectedUser pUser = protectedUserService.registerOrLogin(request.deviceId());

        String accessToken = jwtTokenProvider.createAccessToken(pUser.getId(), "PROTECTED");
        String refreshToken = jwtTokenProvider.createRefreshToken();

        tokenService.saveOrUpdateRefreshToken(null, pUser, refreshToken);

        return ResponseEntity.ok(new RegisterResponse(accessToken, refreshToken, pUser.getLinkingCode())); // 💡 타입 변경
    }
}