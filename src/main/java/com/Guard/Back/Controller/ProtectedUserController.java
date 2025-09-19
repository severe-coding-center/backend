package com.Guard.Back.Controller;

import com.Guard.Back.Domain.ProtectedUser;
import com.Guard.Back.Dto.ProtectedUserDto.*;
import com.Guard.Back.Jwt.JwtTokenProvider;
import com.Guard.Back.Service.ProtectedUserService;
import com.Guard.Back.Service.TokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 피보호자(ProtectedUser)의 등록 및 로그인 관련 API 요청을 처리하는 컨트롤러.
 */
@RestController
@RequestMapping("/api/protected")
@RequiredArgsConstructor
public class ProtectedUserController {

    private final ProtectedUserService protectedUserService;
    private final JwtTokenProvider jwtTokenProvider;
    private final TokenService tokenService;

    /**
     * 피보호자 등록 및 자동 로그인 API.
     * @param request 기기 고유 ID
     * @return 토큰과 연동 코드를 담은 응답
     */
    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@RequestBody RegisterRequest request) {
        // 1. ProtectedUserService를 통해 기기 ID 기반으로 사용자를 등록하거나 조회합니다.
        ProtectedUser pUser = protectedUserService.registerOrLogin(request.deviceId());

        // 2. JwtTokenProvider를 통해 토큰들을 생성합니다.
        String accessToken = jwtTokenProvider.createAccessToken(pUser.getId(), "PROTECTED");
        String refreshToken = jwtTokenProvider.createRefreshToken();

        // 3. TokenService를 통해 RefreshToken을 DB에 저장/갱신합니다.
        tokenService.saveOrUpdateRefreshToken(null, pUser, refreshToken);

        // 4. 생성된 토큰과 연동 코드를 클라이언트에게 반환합니다.
        return ResponseEntity.ok(new RegisterResponse(accessToken, refreshToken, pUser.getLinkingCode()));
    }
}