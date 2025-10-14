package com.Guard.Back.Service;

import com.Guard.Back.Domain.ProtectedUser;
import com.Guard.Back.Domain.User;
import com.Guard.Back.Exception.CustomException;
import com.Guard.Back.Exception.ErrorCode;
import com.Guard.Back.Repository.ProtectedUserRepository;
import com.Guard.Back.Repository.RefreshTokenRepository;
import com.Guard.Back.Repository.RelationshipRepository;
import com.Guard.Back.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final ProtectedUserRepository protectedUserRepository;
    private final RelationshipRepository relationshipRepository;
    private final RefreshTokenRepository refreshTokenRepository;

    @Transactional
    public void deleteGuardian(Long guardianId) {
        User guardian = userRepository.findById(guardianId)
                .orElseThrow(() -> new CustomException(ErrorCode.GUARDIAN_NOT_FOUND));

        relationshipRepository.deleteAllByGuardian(guardian);
        refreshTokenRepository.findByUser(guardian).ifPresent(refreshTokenRepository::delete);
        userRepository.delete(guardian);
    }

    @Transactional
    public void deleteProtectedUser(Long protectedUserId) {
        ProtectedUser protectedUser = protectedUserRepository.findById(protectedUserId)
                .orElseThrow(() -> new CustomException(ErrorCode.PROTECTED_USER_NOT_FOUND));

        relationshipRepository.deleteAllByProtectedUser(protectedUser);
        refreshTokenRepository.findByProtectedUser(protectedUser).ifPresent(refreshTokenRepository::delete);
        protectedUserRepository.delete(protectedUser);
    }
}