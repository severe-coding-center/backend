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

        // 1. 먼저 현재 연결된 보호자 수를 확인합니다.
        long existingGuardians = relationshipRepository.countByProtectedUser(protectedUser);

        // 2. 만약 이미 2명 이상이라면, 예외를 발생시켜 연동을 막습니다.
        if (existingGuardians >= 2) {
            throw new IllegalStateException("이미 최대 2명의 보호자가 연결되어 있습니다.");
        }

        // 3. 관계 엔티티 생성 및 저장
        relationshipRepository.save(
                Relationship.builder()
                        .guardian(guardian)
                        .protectedUser(protectedUser)
                        .build()
        );

        // 4. 이번 연결로 총 2명이 채워졌을 경우에만 연동 코드를 null로 만듭니다.
        if (existingGuardians == 1) {
            protectedUser.setLinkingCode(null);
        }
    }
}