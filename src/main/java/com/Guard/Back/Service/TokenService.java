package com.Guard.Back.Service;

import com.Guard.Back.Domain.ProtectedUser;
import com.Guard.Back.Domain.RefreshToken;
import com.Guard.Back.Domain.User;
import com.Guard.Back.Domain.UserRole;
import com.Guard.Back.Dto.AuthDto;
import com.Guard.Back.Exception.CustomException;
import com.Guard.Back.Exception.ErrorCode;
import com.Guard.Back.Jwt.JwtTokenProvider;
import com.Guard.Back.Repository.ProtectedUserRepository;
import com.Guard.Back.Repository.RefreshTokenRepository;
import com.Guard.Back.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/*JWT Refresh Token의 저장, 재발급, 삭제 등 관리 비즈니스 로직을 처리하는 서비스 클래스.*/
@Service
@RequiredArgsConstructor
@Slf4j
public class TokenService {
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    private final ProtectedUserRepository protectedUserRepository;

    /**
     * 사용자의 Refresh Token을 데이터베이스에 저장하거나 갱신합니다.
     *
     * @param user          토큰의 주인인 보호자 (피보호자일 경우 null).
     * @param protectedUser 토큰의 주인인 피보호자 (보호자일 경우 null).
     * @param tokenValue    저장할 Refresh Token 값.
     */
    @Transactional
    public void saveOrUpdateRefreshToken(User user, ProtectedUser protectedUser, String tokenValue) {
        if (user != null) {
            log.info("[리프레시 토큰 저장] 보호자 ID: {}의 토큰을 저장/갱신합니다.", user.getId());
            refreshTokenRepository.findByUser(user).ifPresentOrElse(
                    token -> token.updateToken(tokenValue),
                    () -> refreshTokenRepository.save(RefreshToken.builder().user(user).tokenValue(tokenValue).build())
            );
        } else if (protectedUser != null) {
            log.info("[리프레시 토큰 저장] 피보호자 ID: {}의 토큰을 저장/갱신합니다.", protectedUser.getId());
            refreshTokenRepository.findByProtectedUser(protectedUser).ifPresentOrElse(
                    token -> token.updateToken(tokenValue),
                    () -> refreshTokenRepository.save(RefreshToken.builder().protectedUser(protectedUser).tokenValue(tokenValue).build())
            );
        }
    }

    /**
     * 유효한 Refresh Token을 받아 새로운 Access Token과 Refresh Token을 재발급합니다.
     *
     * @param refreshToken 재발급을 요청하는 기존 Refresh Token.
     * @return 새로운 Access Token과 Refresh Token이 담긴 DTO.
     * @throws CustomException Refresh Token이 유효하지 않거나 주인을 찾을 수 없는 경우 발생.
     */
    @Transactional
    public AuthDto.RefreshResponse reissueTokens(String refreshToken) {
        log.info("[토큰 재발급] Refresh Token을 이용한 토큰 재발급을 시작합니다.");
        RefreshToken storedToken = refreshTokenRepository.findByTokenValue(refreshToken)
                .orElseThrow(() -> {
                    log.warn("[토큰 재발급] DB에 존재하지 않는 유효하지 않은 Refresh Token으로 재발급 시도.");
                    return new CustomException(ErrorCode.INVALID_REFRESH_TOKEN);
                });

        String newAccessToken;
        String newRefreshToken = jwtTokenProvider.createRefreshToken();
        Long userId = null;

        if (storedToken.getUser() != null) {
            User user = storedToken.getUser();
            userId = user.getId();
            newAccessToken = jwtTokenProvider.createAccessToken(userId, UserRole.GUARDIAN);
        } else if (storedToken.getProtectedUser() != null) {
            ProtectedUser protectedUser = storedToken.getProtectedUser();
            userId = protectedUser.getId();
            newAccessToken = jwtTokenProvider.createAccessToken(userId, UserRole.PROTECTED);
        } else {
            log.error("[토큰 재발급] 주인이 없는 Refresh Token(ID: {})이 발견되었습니다.", storedToken.getId());
            throw new CustomException(ErrorCode.USER_NOT_FOUND);
        }

        storedToken.updateToken(newRefreshToken);
        log.info("[토큰 재발급] 사용자 ID: {}의 토큰이 성공적으로 재발급되었습니다.", userId);
        return new AuthDto.RefreshResponse(newAccessToken, newRefreshToken);
    }

    /**
     * 사용자의 로그아웃을 처리합니다. DB에 저장된 Refresh Token을 삭제합니다.
     *
     * @param userId 로그아웃할 사용자의 ID.
     * @param role   로그아웃할 사용자의 역할 ("ROLE_GUARDIAN" 또는 "ROLE_PROTECTED").
     */
    @Transactional
    public void logout(Long userId, String role) {
        log.info("[로그아웃] 사용자 ID: {}, 역할: {}의 Refresh Token 삭제를 시작합니다.", userId, role);
        if (UserRole.GUARDIAN.getKey().equals(role)) {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new CustomException(ErrorCode.GUARDIAN_NOT_FOUND));
            refreshTokenRepository.findByUser(user).ifPresent(refreshTokenRepository::delete);
        } else if (UserRole.PROTECTED.getKey().equals(role)) {
            ProtectedUser protectedUser = protectedUserRepository.findById(userId)
                    .orElseThrow(() -> new CustomException(ErrorCode.PROTECTED_USER_NOT_FOUND));
            refreshTokenRepository.findByProtectedUser(protectedUser).ifPresent(refreshTokenRepository::delete);
        }
        log.info("[로그아웃] 사용자 ID: {}의 Refresh Token이 성공적으로 삭제되었습니다.", userId);
    }
}