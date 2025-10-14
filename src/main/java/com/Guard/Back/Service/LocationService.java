package com.Guard.Back.Service;

import com.Guard.Back.Domain.LocationLog;
import com.Guard.Back.Domain.ProtectedUser;
import com.Guard.Back.Domain.User;
import com.Guard.Back.Dto.LocationRequest;
import com.Guard.Back.Dto.LocationResponse;
import com.Guard.Back.Exception.CustomException;
import com.Guard.Back.Exception.ErrorCode;
import com.Guard.Back.Repository.LocationLogRepository;
import com.Guard.Back.Repository.ProtectedUserRepository;
import com.Guard.Back.Repository.RelationshipRepository;
import com.Guard.Back.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class LocationService {

    private final LocationLogRepository locationLogRepository;
    private final ProtectedUserRepository protectedUserRepository;
    private final UserRepository userRepository;
    private final RelationshipRepository relationshipRepository;

    @Transactional
    public void saveLocation(Long protectedUserId, LocationRequest request) {
        ProtectedUser protectedUser = protectedUserRepository.findById(protectedUserId)
                .orElseThrow(() -> new CustomException(ErrorCode.PROTECTED_USER_NOT_FOUND));

        LocationLog newLog = LocationLog.builder()
                .protectedUser(protectedUser)
                .latitude(request.latitude())
                .longitude(request.longitude())
                .recordedAt(request.recordedAt() != null ? request.recordedAt() : LocalDateTime.now())
                .build();

        locationLogRepository.save(newLog);
    }

    @Transactional(readOnly = true)
    public LocationResponse getLatestLocation(Long protectedUserId, Long guardianId) {
        User guardian = userRepository.findById(guardianId)
                .orElseThrow(() -> new CustomException(ErrorCode.GUARDIAN_NOT_FOUND));
        ProtectedUser protectedUser = protectedUserRepository.findById(protectedUserId)
                .orElseThrow(() -> new CustomException(ErrorCode.PROTECTED_USER_NOT_FOUND));

        if (!relationshipRepository.existsByGuardianAndProtectedUser(guardian, protectedUser)) {
            throw new CustomException(ErrorCode.UNAUTHORIZED_ACCESS);
        }

        return locationLogRepository.findTopByProtectedUserOrderByIdDesc(protectedUser)
                .map(log -> new LocationResponse(log.getLatitude(), log.getLongitude(), log.getRecordedAt()))
                .orElse(null);
    }
}