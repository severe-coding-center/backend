package com.Guard.Back.Service;

import com.Guard.Back.Domain.ProtectedUser;
import com.Guard.Back.Domain.User;
import com.Guard.Back.Dto.GeofenceDto;
import com.Guard.Back.Exception.CustomException;
import com.Guard.Back.Exception.ErrorCode;
import com.Guard.Back.Repository.ProtectedUserRepository;
import com.Guard.Back.Repository.RelationshipRepository;
import com.Guard.Back.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GeofenceService {
    private final UserRepository userRepository;
    private final ProtectedUserRepository protectedUserRepository;
    private final RelationshipRepository relationshipRepository;

    // 권한 확인 (보호자와 피보호자가 연결되어 있는지)
    private void validateRelationship(Long guardianId, ProtectedUser protectedUser) {
        User guardian = userRepository.findById(guardianId)
                .orElseThrow(() -> new CustomException(ErrorCode.GUARDIAN_NOT_FOUND));
        if (!relationshipRepository.existsByGuardianAndProtectedUser(guardian, protectedUser)) {
            throw new CustomException(ErrorCode.UNAUTHORIZED_ACCESS);
        }
    }

    @Transactional
    public void setGeofence(Long guardianId, Long protectedUserId, GeofenceDto dto) {
        ProtectedUser protectedUser = protectedUserRepository.findById(protectedUserId)
                .orElseThrow(() -> new CustomException(ErrorCode.PROTECTED_USER_NOT_FOUND));
        validateRelationship(guardianId, protectedUser);

        protectedUser.setHomeLatitude(dto.latitude());
        protectedUser.setHomeLongitude(dto.longitude());
        protectedUser.setGeofenceRadius(dto.radius());
    }

    @Transactional
    public void clearGeofence(Long guardianId, Long protectedUserId) {
        ProtectedUser protectedUser = protectedUserRepository.findById(protectedUserId)
                .orElseThrow(() -> new CustomException(ErrorCode.PROTECTED_USER_NOT_FOUND));
        validateRelationship(guardianId, protectedUser);

        protectedUser.setHomeLatitude(null);
        protectedUser.setHomeLongitude(null);
        protectedUser.setGeofenceRadius(null);
    }
}