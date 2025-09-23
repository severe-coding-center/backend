package com.Guard.Back.Service;

import com.Guard.Back.Domain.ProtectedUser;
import com.Guard.Back.Domain.User;
import com.Guard.Back.Repository.ProtectedUserRepository;
import com.Guard.Back.Repository.RefreshTokenRepository;
import com.Guard.Back.Repository.RelationshipRepository;
import com.Guard.Back.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 회원 탈퇴 등 사용자 계정 관리 비즈니스 로직을 처리하는 서비스 클래스.
 */
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final ProtectedUserRepository protectedUserRepository;
    private final RelationshipRepository relationshipRepository;
    private final RefreshTokenRepository refreshTokenRepository;

    /**
     * 보호자 계정을 탈퇴 처리합니다.
     * @param guardianId 탈퇴할 보호자의 ID
     */
    @Transactional
    public void deleteGuardian(Long guardianId) {
        User guardian = userRepository.findById(guardianId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 보호자입니다."));

        // 1. 이 보호자와 연결된 모든 관계(Relationship)를 먼저 삭제합니다.
        relationshipRepository.deleteAllByGuardian(guardian);

        // 2. 이 보호자의 RefreshToken을 삭제합니다.
        refreshTokenRepository.findByUser(guardian).ifPresent(refreshTokenRepository::delete);

        // 3. 마지막으로 보호자 계정을 삭제합니다.
        userRepository.delete(guardian);
    }

    /**
     * 피보호자 계정을 탈퇴 처리합니다.
     * @param protectedUserId 탈퇴할 피보호자의 ID
     */
    @Transactional
    public void deleteProtectedUser(Long protectedUserId) {
        ProtectedUser protectedUser = protectedUserRepository.findById(protectedUserId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 피보호자입니다."));

        // 1. 이 피보호자와 연결된 모든 관계(Relationship)를 먼저 삭제합니다.
        relationshipRepository.deleteAllByProtectedUser(protectedUser);

        // 2. 이 피보호자의 RefreshToken을 삭제합니다.
        refreshTokenRepository.findByProtectedUser(protectedUser).ifPresent(refreshTokenRepository::delete);

        // 3. 마지막으로 피보호자 계정을 삭제합니다.
        protectedUserRepository.delete(protectedUser);
    }
}