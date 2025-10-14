package com.Guard.Back.Repository;

import com.Guard.Back.Domain.RefreshToken;
import com.Guard.Back.Domain.User;
import com.Guard.Back.Domain.ProtectedUser;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

/**
 * RefreshToken 엔티티에 대한 데이터 접근을 처리하는 Repository 인터페이스.
 */
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    /**
     * 토큰 값(문자열)으로 RefreshToken 정보를 조회합니다.
     *
     * @param tokenValue 조회할 Refresh Token 값.
     * @return 해당 토큰 값에 일치하는 RefreshToken 정보를 담은 Optional 객체.
     */
    Optional<RefreshToken> findByTokenValue(String tokenValue);

    /**
     * 보호자(User) 엔티티로 RefreshToken 정보를 조회합니다.
     *
     * @param user 조회할 보호자 엔티티.
     * @return 해당 보호자에게 발급된 RefreshToken 정보를 담은 Optional 객체.
     */
    Optional<RefreshToken> findByUser(User user);

    /**
     * 피보호자(ProtectedUser) 엔티티로 RefreshToken 정보를 조회합니다.
     *
     * @param protectedUser 조회할 피보호자 엔티티.
     * @return 해당 피보호자에게 발급된 RefreshToken 정보를 담은 Optional 객체.
     */
    Optional<RefreshToken> findByProtectedUser(ProtectedUser protectedUser);
}