package com.Guard.Back.Service;

import com.Guard.Back.Domain.ProtectedUser;
import com.Guard.Back.Domain.Relationship;
import com.Guard.Back.Domain.User;
import com.Guard.Back.Domain.UserRole;
import com.Guard.Back.Exception.CustomException;
import com.Guard.Back.Exception.ErrorCode;
import com.Guard.Back.Repository.ProtectedUserRepository;
import com.Guard.Back.Repository.RelationshipRepository;
import com.Guard.Back.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j; // 💡 Slf4j 임포트 추가
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;

/**
 * 보호자와 피보호자 간의 관계 생성 및 삭제 비즈니스 로직을 처리하는 서비스 클래스.
 */
@Service
@RequiredArgsConstructor
@Slf4j // 💡 로깅을 위한 어노테이션 추가
public class RelationshipService {

    private final UserRepository userRepository;
    private final ProtectedUserRepository protectedUserRepository;
    private final RelationshipRepository relationshipRepository;

    /**
     * 연동 코드를 사용하여 보호자와 피보호자 간의 관계를 생성합니다.
     * 한 명의 피보호자는 최대 2명의 보호자와 연결될 수 있습니다.
     *
     * @param linkingCode   피보호자의 유효한 연동 코드.
     * @param guardianId    관계를 맺으려는 보호자의 ID.
     * @throws CustomException 보호자/피보호자가 존재하지 않거나, 연동 코드가 유효하지 않거나,
     * 이미 관계가 존재하거나, 보호자 수가 2명을 초과하는 경우 발생.
     */
    @Transactional
    public void createRelationship(String linkingCode, Long guardianId) {
        log.info("[관계 생성] 보호자 ID: {}, 연동 코드: '{}' - 관계 생성을 시작합니다.", guardianId, linkingCode);
        User guardian = userRepository.findById(guardianId)
                .orElseThrow(() -> new CustomException(ErrorCode.GUARDIAN_NOT_FOUND));

        ProtectedUser protectedUser = protectedUserRepository.findByLinkingCode(linkingCode)
                .orElseThrow(() -> {
                    log.warn("[관계 생성] 보호자 ID: {}가 유효하지 않은 연동 코드 '{}'를 사용했습니다.", guardianId, linkingCode);
                    return new CustomException(ErrorCode.INVALID_LINKING_CODE);
                });

        if (relationshipRepository.existsByGuardianAndProtectedUser(guardian, protectedUser)) {
            log.warn("[관계 생성] 보호자 ID: {}와 피보호자 ID: {}는 이미 관계가 존재합니다.", guardianId, protectedUser.getId());
            throw new CustomException(ErrorCode.RELATIONSHIP_ALREADY_EXISTS);
        }

        long existingGuardians = relationshipRepository.countByProtectedUser(protectedUser);
        if (existingGuardians >= 2) {
            log.warn("[관계 생성] 피보호자 ID: {}는 이미 보호자 2명이 모두 연결되어 있습니다.", protectedUser.getId());
            throw new CustomException(ErrorCode.MAX_GUARDIANS_REACHED);
        }

        relationshipRepository.save(
                Relationship.builder()
                        .guardian(guardian)
                        .protectedUser(protectedUser)
                        .build()
        );

        // 2명의 보호자가 모두 연결되면, 연동 코드를 비활성화(null) 처리합니다.
        if (existingGuardians + 1 == 2) {
            log.info("[관계 생성] 피보호자 ID: {}의 보호자가 2명이 되어 연동 코드를 비활성화합니다.", protectedUser.getId());
            protectedUser.setLinkingCode(null);
        }
        log.info("[관계 생성] 보호자 ID: {}와 피보호자 ID: {}의 관계가 성공적으로 생성되었습니다.", guardianId, protectedUser.getId());
    }

    /**
     * 특정 관계를 삭제(해제)합니다.
     * 관계에 포함된 보호자 또는 피보호자 본인만 삭제할 수 있습니다.
     *
     * @param relationshipId  삭제할 관계의 고유 ID.
     * @param currentUserId   삭제를 요청한 사용자의 ID.
     * @param currentUserType 삭제를 요청한 사용자의 역할 ("GUARDIAN" 또는 "PROTECTED").
     * @throws CustomException 관계가 존재하지 않거나 삭제 권한이 없는 경우 발생.
     */
    @Transactional
    public void deleteRelationship(Long relationshipId, Long currentUserId, String currentUserType) {
        log.info("[관계 해제] 사용자 ID: {} (역할: {})가 관계 ID: {}의 해제를 시작합니다.", currentUserId, currentUserType, relationshipId);
        Relationship relationship = relationshipRepository.findById(relationshipId)
                .orElseThrow(() -> new CustomException(ErrorCode.RELATIONSHIP_NOT_FOUND));

        // 요청자가 관계의 당사자인지 확인
        boolean isGuardian = UserRole.GUARDIAN.name().equals(currentUserType) && relationship.getGuardian().getId().equals(currentUserId);
        boolean isProtectedUser = UserRole.PROTECTED.name().equals(currentUserType) && relationship.getProtectedUser().getId().equals(currentUserId);

        if (!isGuardian && !isProtectedUser) {
            log.warn("[관계 해제] 권한 없음! 사용자 ID: {}가 관계 ID: {}의 해제를 시도했으나 당사자가 아닙니다.", currentUserId, relationshipId);
            throw new CustomException(ErrorCode.UNAUTHORIZED_ACCESS);
        }

        relationshipRepository.delete(relationship);

        // 관계가 해제되어 보호자가 2명 미만이 되면, 새로운 연동 코드를 발급합니다.
        ProtectedUser protectedUser = relationship.getProtectedUser();
        if (protectedUser.getLinkingCode() == null) {
            String newLinkingCode = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
            log.info("[관계 해제] 피보호자 ID: {}에게 새로운 연동 코드 '{}'를 발급합니다.", protectedUser.getId(), newLinkingCode);
            protectedUser.setLinkingCode(newLinkingCode);
        }
        log.info("[관계 해제] 관계 ID: {}의 해제가 성공적으로 완료되었습니다.", relationshipId);
    }
}