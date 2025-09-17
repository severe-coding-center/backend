package com.Guard.Back.Service;

import com.Guard.Back.Domain.*;
import com.Guard.Back.Repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RelationshipService {
    private final UserRepository userRepository;
    private final RelationshipRepository relationshipRepository;

    @Transactional
    public void createRelationship(String linkingCode, Long guardianId) {
        User guardian = userRepository.findById(guardianId)
                .orElseThrow(() -> new IllegalArgumentException("보호자 정보를 찾을 수 없습니다."));

        User protectedUser = userRepository.findByLinkingCode(linkingCode) // 💡 이 메소드는 UserRepository에 추가해야 합니다.
                .orElseThrow(() -> new IllegalArgumentException("잘못된 연동 코드입니다."));

        // 연동 관계 생성 및 저장
        relationshipRepository.save(
                Relationship.builder()
                        .guardian(guardian)
                        .protectedUser(protectedUser)
                        .build()
        );

        // 💡 [중요] 한번 사용된 코드는 null로 만들어 재사용을 막습니다.
        protectedUser.setLinkingCode(null);
    }
}