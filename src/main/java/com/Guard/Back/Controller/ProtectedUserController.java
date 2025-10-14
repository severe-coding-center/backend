package com.Guard.Back.Controller;

import com.Guard.Back.Domain.ProtectedUser;
import com.Guard.Back.Domain.UserRole;
import com.Guard.Back.Dto.RegisterRequest;
import com.Guard.Back.Dto.RegisterResponse;
import com.Guard.Back.Jwt.JwtTokenProvider;
import com.Guard.Back.Service.ProtectedUserService;
import com.Guard.Back.Service.TokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/*피보호자(ProtectedUser)의 등록/로그인 API 요청을 처리하는 컨트롤러.*/
@RestController
@RequestMapping("/api/protected")
@RequiredArgsConstructor
@Slf4j
public class ProtectedUserController {

    private final ProtectedUserService protectedUserService;
    private final JwtTokenProvider jwtTokenProvider;
    private final TokenService tokenService;

    /**
     * 피보호자의 기기 ID를 받아 등록 또는 로그인을 처리하고, JWT 토큰과 연동 코드를 발급합니다.
     * 이 API는 인증 없이 접근 가능합니다.
     *
     * @param request 요청 DTO. 피보호자의 고유 기기 ID(deviceId)를 포함합니다.
     * @return 성공 시 Access Token, Refresh Token, 연동 코드가 담긴 DTO.
     */
    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@RequestBody RegisterRequest request) {
        log.info("[피보호자 등록/로그인] 기기 ID: {}에 대한 요청을 시작합니다.", request.deviceId());

        ProtectedUser pUser = protectedUserService.registerOrLogin(request.deviceId());

        log.info("[피보호자 등록/로그인] 사용자 ID: {}에 대한 JWT 토큰을 발급합니다.", pUser.getId());
        String accessToken = jwtTokenProvider.createAccessToken(pUser.getId(), UserRole.PROTECTED);
        String refreshToken = jwtTokenProvider.createRefreshToken();

        tokenService.saveOrUpdateRefreshToken(null, pUser, refreshToken);

        log.info("[피보호자 등록/로그인] 기기 ID: {}에 대한 요청이 성공적으로 완료되었습니다.", request.deviceId());
        return ResponseEntity.ok(new RegisterResponse(accessToken, refreshToken, pUser.getLinkingCode()));
    }
}