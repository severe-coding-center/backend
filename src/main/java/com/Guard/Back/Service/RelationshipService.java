package com.Guard.Back.Service;

import com.Guard.Back.Domain.ProtectedUser;
import com.Guard.Back.Domain.Relationship;
import com.Guard.Back.Domain.User;
import com.Guard.Back.Exception.CustomException;
import com.Guard.Back.Exception.ErrorCode;
import com.Guard.Back.Repository.ProtectedUserRepository;
import com.Guard.Back.Repository.RelationshipRepository;
import com.Guard.Back.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RelationshipService {

    private final UserRepository userRepository;
    private final ProtectedUserRepository protectedUserRepository;
    private final RelationshipRepository relationshipRepository;

    @Transactional
    public void createRelationship(String linkingCode, Long guardianId) {
        User guardian = userRepository.findById(guardianId)
                .orElseThrow(() -> new CustomException(ErrorCode.GUARDIAN_NOT_FOUND));

        ProtectedUser protectedUser = protectedUserRepository.findByLinkingCode(linkingCode)
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_LINKING_CODE));

        if (relationshipRepository.existsByGuardianAndProtectedUser(guardian, protectedUser)) {
            throw new CustomException(ErrorCode.RELATIONSHIP_ALREADY_EXISTS);
        }

        long existingGuardians = relationshipRepository.countByProtectedUser(protectedUser);
        if (existingGuardians >= 2) {
            throw new CustomException(ErrorCode.MAX_GUARDIANS_REACHED);
        }

        relationshipRepository.save(
                Relationship.builder()
                        .guardian(guardian)
                        .protectedUser(protectedUser)
                        .build()
        );

        if (existingGuardians + 1 == 2) {
            protectedUser.setLinkingCode(null);
        }
    }

    @Transactional
    public void deleteRelationship(Long relationshipId, Long currentUserId, String currentUserType) {
        Relationship relationship = relationshipRepository.findById(relationshipId)
                .orElseThrow(() -> new CustomException(ErrorCode.RELATIONSHIP_NOT_FOUND));

        boolean isGuardian = "GUARDIAN".equals(currentUserType) && relationship.getGuardian().getId().equals(currentUserId);
        boolean isProtectedUser = "PROTECTED".equals(currentUserType) && relationship.getProtectedUser().getId().equals(currentUserId);

        if (!isGuardian && !isProtectedUser) {
            throw new CustomException(ErrorCode.UNAUTHORIZED_ACCESS);
        }

        relationshipRepository.delete(relationship);

        ProtectedUser protectedUser = relationship.getProtectedUser();
        if (protectedUser.getLinkingCode() == null) {
            String newLinkingCode = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
            protectedUser.setLinkingCode(newLinkingCode);
        }
    }
}