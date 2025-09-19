package com.Guard.Back.Repository;

import com.Guard.Back.Domain.ProtectedUser;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

/**
 * 피보호자(ProtectedUser) 엔티티에 대한 데이터 접근 계층.
 */
public interface ProtectedUserRepository extends JpaRepository<ProtectedUser, Long> {
    /**
     * 기기 ID로 피보호자를 조회합니다.
     * @param deviceId 조회할 기기 ID
     * @return Optional<ProtectedUser>
     */
    Optional<ProtectedUser> findByDeviceId(String deviceId);

    /**
     * 연동 코드로 피보호자를 조회합니다.
     * @param linkingCode 조회할 연동 코드
     * @return Optional<ProtectedUser>
     */
    Optional<ProtectedUser> findByLinkingCode(String linkingCode);
}