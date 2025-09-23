package com.Guard.Back.Service;

import com.Guard.Back.Domain.ProtectedUser;
import com.Guard.Back.Domain.RefreshToken;
import com.Guard.Back.Domain.User;
import com.Guard.Back.Jwt.JwtTokenProvider;
import com.Guard.Back.Repository.ProtectedUserRepository;
import com.Guard.Back.Repository.RefreshTokenRepository;
import com.Guard.Back.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Refresh Token의 저장, 갱신, 검증, 삭제를 전담하는 서비스 클래스.
 * AuthService, ProtectedUserService의 책임을 덜어줍니다.
 */
@Service
@RequiredArgsConstructor
public class TokenService {
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    private final ProtectedUserRepository protectedUserRepository;

    /**
     * 사용자의 Refresh Token을 DB에 저장하거나 갱신합니다.
     * @param user 보호자 객체 (피보호자일 경우 null)
     * @param protectedUser 피보호자 객체 (보호자일 경우 null)
     * @param tokenValue 새로 발급된 Refresh Token 값
     */
    @Transactional
    public void saveOrUpdateRefreshToken(User user, ProtectedUser protectedUser, String tokenValue) {
        if (user != null) {
            // 보호자의 토큰이 이미 존재하는지 확인
            refreshTokenRepository.findByUser(user).ifPresentOrElse(
                    token -> token.updateToken(tokenValue), // 존재하면 값만 갱신
                    () -> refreshTokenRepository.save(RefreshToken.builder().user(user).tokenValue(tokenValue).build()) // 없으면 새로 생성
            );
        } else if (protectedUser != null) {
            // 피보호자의 토큰이 이미 존재하는지 확인
            refreshTokenRepository.findByProtectedUser(protectedUser).ifPresentOrElse(
                    token -> token.updateToken(tokenValue), // 존재하면 값만 갱신
                    () -> refreshTokenRepository.save(RefreshToken.builder().protectedUser(protectedUser).tokenValue(tokenValue).build()) // 없으면 새로 생성
            );
        }
    }
    /**
     * Refresh Token을 사용하여 새로운 Access Token을 재발급
     * @param refreshToken 클라이언트로부터 받은 Refresh Token
     * @return 새로 생성된 Access Token
     */
    @Transactional
    public String reissueAccessToken(String refreshToken) {
        // 1. DB 에서 Refresh Token 정보를 조회하고 유효성을 검증합니다.
        RefreshToken storedToken = refreshTokenRepository.findByTokenValue(refreshToken)
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 Refresh Token 입니다."));

        // 2. 토큰이 보호자의 것인지 피보호자의 것인지 확인합니다.
        if (storedToken.getUser() != null) {
            User user = storedToken.getUser();
            return jwtTokenProvider.createAccessToken(user.getId(), "GUARDIAN");
        } else if (storedToken.getProtectedUser() != null) {
            ProtectedUser protectedUser = storedToken.getProtectedUser();
            return jwtTokenProvider.createAccessToken(protectedUser.getId(), "PROTECTED");
        } else {
            throw new IllegalStateException("토큰에 연결된 사용자가 없습니다.");
        }
    }
    /**
     * 사용자의 Refresh Token을 DB에서 삭제하여 로그아웃 처리.
     * @param userId 로그아웃을 요청한 사용자의 ID
     * @param userType 사용자의 타입 ("GUARDIAN" 또는 "PROTECTED")
     */
    @Transactional
    public void logout(Long userId, String userType) {
        if ("GUARDIAN".equals(userType)) {
            User user = userRepository.findById(userId).orElseThrow();
            refreshTokenRepository.findByUser(user).ifPresent(refreshTokenRepository::delete);
        } else if ("PROTECTED".equals(userType)) {
            ProtectedUser protectedUser = protectedUserRepository.findById(userId).orElseThrow();
            refreshTokenRepository.findByProtectedUser(protectedUser).ifPresent(refreshTokenRepository::delete);
        }
    }


}