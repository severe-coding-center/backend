package com.Guard.Back.Service;

import com.Guard.Back.Domain.ProtectedUser;
import com.Guard.Back.Domain.RefreshToken;
import com.Guard.Back.Domain.User;
import com.Guard.Back.Repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Refresh Token의 저장 및 갱신을 전담하는 서비스 클래스.
 * AuthService, ProtectedUserService의 책임을 덜어줍니다.
 */
@Service
@RequiredArgsConstructor
public class TokenService {
    private final RefreshTokenRepository refreshTokenRepository;

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
}