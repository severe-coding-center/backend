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

    // 맵 다시 켰을 때 안전 반경 위치 불러오는 메소드
    @Transactional(readOnly = true)
    public GeofenceDto getGeofence(Long guardianId, Long protectedUserId) {
        ProtectedUser protectedUser = protectedUserRepository.findById(protectedUserId)
                .orElseThrow(() -> new CustomException(ErrorCode.PROTECTED_USER_NOT_FOUND));
        validateRelationship(guardianId, protectedUser); // 기존 권한 검증 로직 재사용

        // 설정된 지오펜스가 없으면 null을 반환
        if (protectedUser.getHomeLatitude() == null) {
            return null;
        }

        // 설정된 위도, 경도, 반경 정보를 DTO에 담아 반환
        return new GeofenceDto(
                protectedUser.getHomeLatitude(),
                protectedUser.getHomeLongitude(),
                protectedUser.getGeofenceRadius()
        );
    }}