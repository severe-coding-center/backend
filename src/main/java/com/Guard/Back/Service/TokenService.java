package com.Guard.Back.Service;

import com.Guard.Back.Domain.ProtectedUser;
import com.Guard.Back.Domain.RefreshToken;
import com.Guard.Back.Domain.User;
import com.Guard.Back.Domain.UserRole; // 💡 import 추가
import com.Guard.Back.Dto.AuthDto;
import com.Guard.Back.Exception.CustomException;
import com.Guard.Back.Exception.ErrorCode;
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

    // ... saveOrUpdateRefreshToken 메소드는 기존과 동일 ...

    @Transactional
    public AuthDto.RefreshResponse reissueTokens(String refreshToken) {
        RefreshToken storedToken = refreshTokenRepository.findByTokenValue(refreshToken)
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_REFRESH_TOKEN));

        String newAccessToken;
        String newRefreshToken = jwtTokenProvider.createRefreshToken();

        if (storedToken.getUser() != null) {
            User user = storedToken.getUser();
            // 💡 [수정] UserRole.GUARDIAN Enum 사용
            newAccessToken = jwtTokenProvider.createAccessToken(user.getId(), UserRole.GUARDIAN);
        } else if (storedToken.getProtectedUser() != null) {
            ProtectedUser protectedUser = storedToken.getProtectedUser();
            // 💡 [수정] UserRole.PROTECTED Enum 사용
            newAccessToken = jwtTokenProvider.createAccessToken(protectedUser.getId(), UserRole.PROTECTED);
        } else {
            throw new CustomException(ErrorCode.USER_NOT_FOUND);
        }

        storedToken.updateToken(newRefreshToken);

        return new AuthDto.RefreshResponse(newAccessToken, newRefreshToken);
    }

    @Transactional
    public void logout(Long userId, String role) { // 💡 파라미터 이름 변경 (userType -> role)
        // 💡 [수정] UserRole Enum의 key 값과 비교합니다.
        if (UserRole.GUARDIAN.getKey().equals(role)) {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new CustomException(ErrorCode.GUARDIAN_NOT_FOUND));
            refreshTokenRepository.findByUser(user).ifPresent(refreshTokenRepository::delete);
        } else if (UserRole.PROTECTED.getKey().equals(role)) {
            ProtectedUser protectedUser = protectedUserRepository.findById(userId)
                    .orElseThrow(() -> new CustomException(ErrorCode.PROTECTED_USER_NOT_FOUND));
            refreshTokenRepository.findByProtectedUser(protectedUser).ifPresent(refreshTokenRepository::delete);
        }
    }
}