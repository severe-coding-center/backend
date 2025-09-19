package com.Guard.Back.Repository;

import com.Guard.Back.Domain.RefreshToken;
import com.Guard.Back.Domain.User;
import com.Guard.Back.Domain.ProtectedUser;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

/**
 * RefreshToken 엔티티에 대한 데이터 접근 계층.
 */
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    /**
     * 토큰 값으로 RefreshToken 정보를 조회합니다.
     * @param tokenValue 조회할 토큰 값
     * @return Optional<RefreshToken>
     */
    Optional<RefreshToken> findByTokenValue(String tokenValue);

    /**
     * 보호자(User) 객체로 RefreshToken 정보를 조회합니다.
     * @param user 조회할 보호자 객체
     * @return Optional<RefreshToken>
     */
    Optional<RefreshToken> findByUser(User user);

    /**
     * 피보호자(ProtectedUser) 객체로 RefreshToken 정보를 조회합니다.
     * @param protectedUser 조회할 피보호자 객체
     * @return Optional<RefreshToken>
     */
    Optional<RefreshToken> findByProtectedUser(ProtectedUser protectedUser);
}