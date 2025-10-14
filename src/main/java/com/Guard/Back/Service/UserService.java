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
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 사용자 계정(보호자, 피보호자) 삭제와 관련된 비즈니스 로직을 처리하는 서비스 클래스.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final ProtectedUserRepository protectedUserRepository;
    private final RelationshipRepository relationshipRepository;
    private final RefreshTokenRepository refreshTokenRepository;

    /**
     * 특정 보호자 계정을 탈퇴시킵니다.
     * 연관된 모든 관계, 리프레시 토큰이 함께 삭제됩니다.
     *
     * @param guardianId 삭제할 보호자의 ID.
     * @throws CustomException 보호자가 존재하지 않을 경우 발생.
     */
    @Transactional
    public void deleteGuardian(Long guardianId) {
        log.info("[회원 탈퇴] 보호자 ID: {}의 계정 삭제를 시작합니다.", guardianId);
        User guardian = userRepository.findById(guardianId)
                .orElseThrow(() -> {
                    log.error("[회원 탈퇴] 존재하지 않는 보호자 ID({})에 대한 탈퇴가 요청되었습니다.", guardianId);
                    return new CustomException(ErrorCode.GUARDIAN_NOT_FOUND);
                });

        log.debug("[회원 탈퇴] 보호자 ID: {}와 연결된 모든 관계를 삭제합니다.", guardianId);
        relationshipRepository.deleteAllByGuardian(guardian);

        log.debug("[회원 탈퇴] 보호자 ID: {}의 리프레시 토큰을 삭제합니다.", guardianId);
        refreshTokenRepository.findByUser(guardian).ifPresent(refreshTokenRepository::delete);

        userRepository.delete(guardian);
        log.info("[회원 탈퇴] 보호자 ID: {}의 계정 삭제가 성공적으로 완료되었습니다.", guardianId);
    }

    /**
     * 특정 피보호자 계정을 탈퇴시킵니다.
     * 연관된 모든 관계, 리프레시 토큰, 위치 기록이 함께 삭제됩니다. (위치 기록은 Cascade 설정에 의해 자동 삭제됨)
     *
     * @param protectedUserId 삭제할 피보호자의 ID.
     * @throws CustomException 피보호자가 존재하지 않을 경우 발생.
     */
    @Transactional
    public void deleteProtectedUser(Long protectedUserId) {
        log.info("[회원 탈퇴] 피보호자 ID: {}의 계정 삭제를 시작합니다.", protectedUserId);
        ProtectedUser protectedUser = protectedUserRepository.findById(protectedUserId)
                .orElseThrow(() -> {
                    log.error("[회원 탈퇴] 존재하지 않는 피보호자 ID({})에 대한 탈퇴가 요청되었습니다.", protectedUserId);
                    return new CustomException(ErrorCode.PROTECTED_USER_NOT_FOUND);
                });

        log.debug("[회원 탈퇴] 피보호자 ID: {}와 연결된 모든 관계를 삭제합니다.", protectedUserId);
        relationshipRepository.deleteAllByProtectedUser(protectedUser);

        log.debug("[회원 탈퇴] 피보호자 ID: {}의 리프레시 토큰을 삭제합니다.", protectedUserId);
        refreshTokenRepository.findByProtectedUser(protectedUser).ifPresent(refreshTokenRepository::delete);

        protectedUserRepository.delete(protectedUser);
        log.info("[회원 탈퇴] 피보호자 ID: {}의 계정 삭제가 성공적으로 완료되었습니다.", protectedUserId);
    }
}