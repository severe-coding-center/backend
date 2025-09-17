package com.Guard.Back.Repository;

import com.Guard.Back.Domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByPhoneNumber(String phoneNumber);
    Optional<User> findByLinkingCode(String linkingCode);
}