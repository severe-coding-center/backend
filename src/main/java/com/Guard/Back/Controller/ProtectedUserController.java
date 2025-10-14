package com.Guard.Back.Controller;

import com.Guard.Back.Domain.ProtectedUser;
import com.Guard.Back.Domain.UserRole; // 💡 import 추가
import com.Guard.Back.Dto.RegisterRequest;
import com.Guard.Back.Dto.RegisterResponse;
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
    public ResponseEntity<RegisterResponse> register(@RequestBody RegisterRequest request) {
        ProtectedUser pUser = protectedUserService.registerOrLogin(request.deviceId());

        // 💡 [수정] "PROTECTED" 문자열 대신 UserRole.PROTECTED Enum을 사용합니다.
        String accessToken = jwtTokenProvider.createAccessToken(pUser.getId(), UserRole.PROTECTED);
        String refreshToken = jwtTokenProvider.createRefreshToken();

        tokenService.saveOrUpdateRefreshToken(null, pUser, refreshToken);

        return ResponseEntity.ok(new RegisterResponse(accessToken, refreshToken, pUser.getLinkingCode()));
    }
}