package com.Guard.Back.Service;

import com.Guard.Back.Domain.ProtectedUser;
import com.Guard.Back.Domain.RefreshToken;
import com.Guard.Back.Domain.User;
import com.Guard.Back.Dto.AuthDto; // 💡 DTO 임포트
import com.Guard.Back.Jwt.JwtTokenProvider;
import com.Guard.Back.Repository.ProtectedUserRepository;
import com.Guard.Back.Repository.RefreshTokenRepository;
import com.Guard.Back.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TokenService {
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    private final ProtectedUserRepository protectedUserRepository;

    @Transactional
    public void saveOrUpdateRefreshToken(User user, ProtectedUser protectedUser, String tokenValue) {
        if (user != null) {
            refreshTokenRepository.findByUser(user).ifPresentOrElse(
                    token -> token.updateToken(tokenValue),
                    () -> refreshTokenRepository.save(RefreshToken.builder().user(user).tokenValue(tokenValue).build())
            );
        } else if (protectedUser != null) {
            refreshTokenRepository.findByProtectedUser(protectedUser).ifPresentOrElse(
                    token -> token.updateToken(tokenValue),
                    () -> refreshTokenRepository.save(RefreshToken.builder().protectedUser(protectedUser).tokenValue(tokenValue).build())
            );
        }
    }

    /**
     * 💡 [수정] Refresh Token을 사용하여 새로운 Access Token과 Refresh Token을 모두 재발급 (Rotation)
     * @param refreshToken 클라이언트로부터 받은 Refresh Token
     * @return 새로 생성된 Access Token과 Refresh Token을 담은 DTO
     */
    @Transactional
    public AuthDto.RefreshResponse reissueTokens(String refreshToken) {
        // 1. DB 에서 Refresh Token 정보를 조회하고 유효성을 검증합니다.
        RefreshToken storedToken = refreshTokenRepository.findByTokenValue(refreshToken)
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 Refresh Token 입니다."));

        // 2. 새로운 토큰들을 생성합니다.
        String newAccessToken;
        String newRefreshToken = jwtTokenProvider.createRefreshToken();

        // 3. 토큰이 보호자의 것인지 피보호자의 것인지 확인하고 새 AccessToken을 만듭니다.
        if (storedToken.getUser() != null) {
            User user = storedToken.getUser();
            newAccessToken = jwtTokenProvider.createAccessToken(user.getId(), "GUARDIAN");
        } else if (storedToken.getProtectedUser() != null) {
            ProtectedUser protectedUser = storedToken.getProtectedUser();
            newAccessToken = jwtTokenProvider.createAccessToken(protectedUser.getId(), "PROTECTED");
        } else {
            throw new IllegalStateException("토큰에 연결된 사용자가 없습니다.");
        }

        // 4. 💡 [핵심] DB에 저장된 기존 Refresh Token 값을 새로운 값으로 갱신합니다. (Rotation)
        storedToken.updateToken(newRefreshToken);

        // 5. 새로 발급된 토큰들을 DTO에 담아 반환합니다.
        return new AuthDto.RefreshResponse(newAccessToken, newRefreshToken);
    }

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