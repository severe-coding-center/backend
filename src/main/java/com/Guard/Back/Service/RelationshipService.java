package com.Guard.Back.Service;

import com.Guard.Back.Domain.ProtectedUser;
import com.Guard.Back.Domain.Relationship;
import com.Guard.Back.Domain.User;
import com.Guard.Back.Repository.ProtectedUserRepository;
import com.Guard.Back.Repository.RelationshipRepository;
import com.Guard.Back.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * 보호자와 피보호자 간의 관계 설정 비즈니스 로직을 처리하는 서비스 클래스.
 */
@Service
@RequiredArgsConstructor
public class RelationshipService {

    private final UserRepository userRepository;
    private final ProtectedUserRepository protectedUserRepository;
    private final RelationshipRepository relationshipRepository;

    /**
     * 연동 코드를 사용하여 보호자와 피보호자를 연결합니다.
     * 한 명의 피보호자에게는 최대 2명의 보호자만 연결될 수 있습니다.
     * @param linkingCode 피보호자의 6자리 연동 코드
     * @param guardianId 연동을 요청한 보호자의 ID (JWT에서 추출)
     */
    @Transactional
    public void createRelationship(String linkingCode, Long guardianId) {
        User guardian = userRepository.findById(guardianId)
                .orElseThrow(() -> new IllegalArgumentException("보호자 정보를 찾을 수 없습니다."));
        ProtectedUser protectedUser = protectedUserRepository.findByLinkingCode(linkingCode)
                .orElseThrow(() -> new IllegalArgumentException("잘못된 연동 코드입니다."));

        // 💡 1. [수정] 이미 두 사람의 관계가 존재하는지 먼저 확인
        if (relationshipRepository.existsByGuardianAndProtectedUser(guardian, protectedUser)) {
            throw new IllegalStateException("이미 연결된 관계입니다.");
        }

        // 💡 2. [기존 로직] 피보호자에게 연결된 총 보호자 수 확인
        long existingGuardians = relationshipRepository.countByProtectedUser(protectedUser);
        if (existingGuardians >= 2) {
            throw new IllegalStateException("이미 최대 2명의 보호자가 연결되어 있습니다.");
        }

        // 💡 3. [기존 로직] 새로운 관계 저장
        relationshipRepository.save(
                Relationship.builder()
                        .guardian(guardian)
                        .protectedUser(protectedUser)
                        .build()
        );

        // 💡 4. [기존 로직] 연결 후 연동 코드 처리
        if (existingGuardians + 1 == 2) { // 이번 연결로 총 2명이 채워졌을 경우
            protectedUser.setLinkingCode(null);
            // protectedUserRepository.save(protectedUser); // @Transactional 이므로 자동 변경 감지(dirty checking) 되어 저장됩니다.
        }
    }

    /**
     * 특정 관계를 해제합니다.
     * @param relationshipId 해제할 관계의 ID
     * @param currentUserId 요청을 보낸 사용자의 ID (JWT에서 추출)
     * @param currentUserType 요청을 보낸 사용자의 타입 ("GUARDIAN" 또는 "PROTECTED")
     */
    @Transactional
    public void deleteRelationship(Long relationshipId, Long currentUserId, String currentUserType) {
        Relationship relationship = relationshipRepository.findById(relationshipId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 관계입니다."));

        // 💡 [핵심] 권한 검증: 요청자가 해당 관계의 보호자 또는 피보호자인지 확인합니다.
        boolean isGuardian = "GUARDIAN".equals(currentUserType) && relationship.getGuardian().getId().equals(currentUserId);
        boolean isProtectedUser = "PROTECTED".equals(currentUserType) && relationship.getProtectedUser().getId().equals(currentUserId);

        if (!isGuardian && !isProtectedUser) {
            throw new IllegalStateException("해당 관계를 해제할 권한이 없습니다.");
        }

        // 권한이 확인되면 관계를 삭제합니다.
        relationshipRepository.delete(relationship);

        // 연결이 해제된 피보호자에게 새로운 연동 코드를 발급해줍니다.
        ProtectedUser protectedUser = relationship.getProtectedUser();
        if (protectedUser.getLinkingCode() == null) {
            String newLinkingCode = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
            protectedUser.setLinkingCode(newLinkingCode);
            protectedUserRepository.save(protectedUser);
        }
    }
}