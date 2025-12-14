package com.Guard.Back.Repository;

import com.Guard.Back.Domain.ProtectedUser;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

/*ProtectedUser(피보호자) 엔티티에 대한 데이터 접근을 처리하는 Repository 인터페이스.*/
public interface ProtectedUserRepository extends JpaRepository<ProtectedUser, Long> {

    /**
     * 기기 고유 ID로 피보호자를 조회
     * @param deviceId 조회할 기기 ID.
     * @return 해당 기기 ID를 가진 피보호자 정보를 담은 Optional 객체.
     */
    Optional<ProtectedUser> findByDeviceId(String deviceId);

    /**
     * 연동 코드로 피보호자를 조회
     *
     * @param linkingCode 조회할 연동 코드.
     * @return 해당 연동 코드를 가진 피보호자 정보를 담은 Optional 객체.
     */
    Optional<ProtectedUser> findByLinkingCode(String linkingCode);
}