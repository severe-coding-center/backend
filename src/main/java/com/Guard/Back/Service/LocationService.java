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
import lombok.extern.slf4j.Slf4j; // 💡 Slf4j 임포트 추가
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

/**
 * 위치 정보 저장 및 조회와 관련된 비즈니스 로직을 처리하는 서비스 클래스.
 */
@Service
@RequiredArgsConstructor
@Slf4j // 💡 로깅을 위한 어노테이션 추가
public class LocationService {

    private final LocationLogRepository locationLogRepository;
    private final ProtectedUserRepository protectedUserRepository;
    private final UserRepository userRepository;
    private final RelationshipRepository relationshipRepository;

    /**
     * 피보호자의 위치 정보를 데이터베이스에 저장합니다.
     *
     * @param protectedUserId 위치를 저장할 피보호자의 ID.
     * @param request         저장할 위치 정보(위도, 경도)를 담은 DTO.
     * @throws CustomException 피보호자가 존재하지 않을 경우 발생.
     */
    @Transactional
    public void saveLocation(Long protectedUserId, LocationRequest request) {
        log.info("[위치저장] 피보호자 ID: {}의 위치 정보 저장을 시작합니다.", protectedUserId);
        ProtectedUser protectedUser = protectedUserRepository.findById(protectedUserId)
                .orElseThrow(() -> {
                    log.error("[위치저장] 존재하지 않는 피보호자 ID({})에 대한 위치 저장 시도.", protectedUserId);
                    return new CustomException(ErrorCode.PROTECTED_USER_NOT_FOUND);
                });

        LocationLog newLog = LocationLog.builder()
                .protectedUser(protectedUser)
                .latitude(request.latitude())
                .longitude(request.longitude())
                .recordedAt(request.recordedAt() != null ? request.recordedAt() : LocalDateTime.now())
                .build();

        locationLogRepository.save(newLog);
        log.info("[위치저장] 피보호자 ID: {}의 위치 정보 저장을 성공적으로 완료했습니다.", protectedUserId);
    }

    /**
     * 특정 피보호자의 가장 최신 위치 정보를 조회합니다.
     * 요청한 보호자가 해당 피보호자와 관계를 맺고 있는지 반드시 확인합니다.
     *
     * @param protectedUserId 조회 대상 피보호자의 ID.
     * @param guardianId      요청을 보낸 보호자의 ID (권한 검증용).
     * @return 최신 위치 정보 DTO. 위치 기록이 없으면 null을 반환합니다.
     * @throws CustomException 보호자/피보호자가 존재하지 않거나, 두 사용자 간의 관계가 없을 경우 발생.
     */
    @Transactional(readOnly = true)
    public LocationResponse getLatestLocation(Long protectedUserId, Long guardianId) {
        log.info("[위치조회] 보호자 ID: {}가 피보호자 ID: {}의 최신 위치 조회를 시작합니다.", guardianId, protectedUserId);
        User guardian = userRepository.findById(guardianId)
                .orElseThrow(() -> {
                    log.error("[위치조회] 존재하지 않는 보호자 ID({})가 위치 조회를 시도했습니다.", guardianId);
                    return new CustomException(ErrorCode.GUARDIAN_NOT_FOUND);
                });
        ProtectedUser protectedUser = protectedUserRepository.findById(protectedUserId)
                .orElseThrow(() -> {
                    log.error("[위치조회] 보호자 ID: {}가 존재하지 않는 피보호자 ID({})의 위치 조회를 시도했습니다.", guardianId, protectedUserId);
                    return new CustomException(ErrorCode.PROTECTED_USER_NOT_FOUND);
                });

        if (!relationshipRepository.existsByGuardianAndProtectedUser(guardian, protectedUser)) {
            log.warn("[위치조회] 권한 없음! 보호자 ID: {}가 관계없는 피보호자 ID: {}의 위치 조회를 시도했습니다.", guardianId, protectedUserId);
            throw new CustomException(ErrorCode.UNAUTHORIZED_ACCESS);
        }

        log.info("[위치조회] 보호자 ID: {}가 요청한 피보호자 ID: {}의 위치 조회가 성공적으로 완료되었습니다.", guardianId, protectedUserId);
        return locationLogRepository.findTopByProtectedUserOrderByIdDesc(protectedUser)
                .map(log -> new LocationResponse(log.getLatitude(), log.getLongitude(), log.getRecordedAt()))
                .orElse(null);
    }
}