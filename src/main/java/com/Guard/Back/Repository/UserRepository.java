package com.Guard.Back.Repository;

import com.Guard.Back.Domain.OAuthProvider;
import com.Guard.Back.Domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

/*User(보호자) 엔티티에 대한 데이터 접근을 처리하는 Repository 인터페이스.*/
public interface UserRepository extends JpaRepository<User, Long> {
    /**
     * 소셜 로그인 제공자 타입과 해당 제공자 내에서의 고유 ID를 사용하여 사용자를 조회합니다.
     * @param provider   소셜 로그인 제공자 (e.g., KAKAO).
     * @param providerId 해당 제공자에서의 사용자 고유 ID.
     * @return 해당 정보와 일치하는 사용자 정보를 담은 Optional 객체.
     */
    Optional<User> findByProviderAndProviderId(OAuthProvider provider, String providerId);
}