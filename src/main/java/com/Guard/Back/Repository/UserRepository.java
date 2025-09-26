package com.Guard.Back.Repository;

import com.Guard.Back.Domain.OAuthProvider;
import com.Guard.Back.Domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    // 소셜로그인 ID로 사용자를 찾는 메소드
    Optional<User> findByProviderAndProviderId(OAuthProvider provider, String providerId);
}