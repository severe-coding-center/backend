package com.Guard.Back.Repository;

import com.Guard.Back.Domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

/**
 * 보호자(User) 엔티티에 대한 데이터 접근 계층.
 */
public interface UserRepository extends JpaRepository<User, Long> {
    /**
     * 휴대폰 번호로 보호자를 조회합니다.
     * @param phoneNumber 조회할 휴대폰 번호
     * @return Optional<User>
     */
    Optional<User> findByPhoneNumber(String phoneNumber);
}