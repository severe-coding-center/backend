package com.Guard.Back.Service;

import com.Guard.Back.Domain.LocationLog;
import com.Guard.Back.Domain.ProtectedUser;
import com.Guard.Back.Dto.LocationLogDto.*;
import com.Guard.Back.Repository.LocationLogRepository;
import com.Guard.Back.Repository.ProtectedUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class LocationService {

    private final LocationLogRepository locationLogRepository;
    private final ProtectedUserRepository protectedUserRepository;

    /**
     * 피보호자의 위치 정보를 저장합니다.
     * @param protectedUserId 위치를 보내는 피보호자의 ID (JWT 토큰에서 추출)
     * @param request 위도, 경도 등 위치 정보
     */
    @Transactional
    public void saveLocation(Long protectedUserId, LocationRequest request) {
        ProtectedUser protectedUser = protectedUserRepository.findById(protectedUserId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 피보호자입니다."));

        LocationLog newLog = LocationLog.builder()
                .protectedUser(protectedUser)
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .recordedAt(request.getRecordedAt() != null ? request.getRecordedAt() : LocalDateTime.now())
                .build();

        locationLogRepository.save(newLog);
    }

    /**
     * 특정 피보호자의 최신 위치를 조회합니다.
     * @param protectedUserId 조회 대상 피보호자의 ID
     * @return 최신 위치 정보 DTO
     */
    @Transactional(readOnly = true)
    public LocationResponse getLatestLocation(Long protectedUserId) {
        ProtectedUser protectedUser = protectedUserRepository.findById(protectedUserId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 피보호자입니다."));

        return locationLogRepository.findTopByProtectedUserOrderByIdDesc(protectedUser)
                .map(log -> new LocationResponse(log.getLatitude(), log.getLongitude(), log.getRecordedAt()))
                .orElse(null); // 위치 기록이 없으면 null 반환
    }
}