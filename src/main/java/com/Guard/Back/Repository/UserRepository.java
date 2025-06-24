package com.Guard.Back.Repository;

import com.Guard.Back.Domain.User;
import com.Guard.Back.Auth.OAuthProvider;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByProviderAndProviderId(OAuthProvider provider, String providerId);
}
