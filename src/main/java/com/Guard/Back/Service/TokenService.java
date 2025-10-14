package com.Guard.Back.Service;

import com.Guard.Back.Domain.ProtectedUser;
import com.Guard.Back.Domain.RefreshToken;
import com.Guard.Back.Domain.User;
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

    @Transactional
    public AuthDto.RefreshResponse reissueTokens(String refreshToken) {
        RefreshToken storedToken = refreshTokenRepository.findByTokenValue(refreshToken)
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_REFRESH_TOKEN));

        String newAccessToken;
        String newRefreshToken = jwtTokenProvider.createRefreshToken();

        if (storedToken.getUser() != null) {
            User user = storedToken.getUser();
            newAccessToken = jwtTokenProvider.createAccessToken(user.getId(), "GUARDIAN");
        } else if (storedToken.getProtectedUser() != null) {
            ProtectedUser protectedUser = storedToken.getProtectedUser();
            newAccessToken = jwtTokenProvider.createAccessToken(protectedUser.getId(), "PROTECTED");
        } else {
            throw new CustomException(ErrorCode.USER_NOT_FOUND);
        }

        storedToken.updateToken(newRefreshToken);

        return new AuthDto.RefreshResponse(newAccessToken, newRefreshToken);
    }

    @Transactional
    public void logout(Long userId, String userType) {
        if ("GUARDIAN".equals(userType)) {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new CustomException(ErrorCode.GUARDIAN_NOT_FOUND));
            refreshTokenRepository.findByUser(user).ifPresent(refreshTokenRepository::delete);
        } else if ("PROTECTED".equals(userType)) {
            ProtectedUser protectedUser = protectedUserRepository.findById(userId)
                    .orElseThrow(() -> new CustomException(ErrorCode.PROTECTED_USER_NOT_FOUND));
            refreshTokenRepository.findByProtectedUser(protectedUser).ifPresent(refreshTokenRepository::delete);
        }
    }
}