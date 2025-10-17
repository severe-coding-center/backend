package com.Guard.Back.Service;

import com.Guard.Back.Domain.*;
import com.Guard.Back.Dto.LocationRequest;
import com.Guard.Back.Dto.LocationResponse;
import com.Guard.Back.Exception.CustomException;
import com.Guard.Back.Exception.ErrorCode;
import com.Guard.Back.Repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 위치 정보 저장, 조회 및 지오펜스 관련 비즈니스 로직을 처리하는 서비스 클래스.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class LocationService {

    private final LocationLogRepository locationLogRepository;
    private final ProtectedUserRepository protectedUserRepository;
    private final UserRepository userRepository;
    private final RelationshipRepository relationshipRepository;
    private final AlertLogRepository alertLogRepository;
    private final FCMService fcmService;

    /**
     * 피보호자의 위치 정보를 데이터베이스에 저장하고, 지오펜스 이탈 여부를 검사합니다.
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

        // 💡 [핵심] 위치 저장 후, 지오펜스 검사 로직을 호출합니다.
        checkGeofence(protectedUser, request.latitude(), request.longitude());

        log.info("[위치저장] 피보호자 ID: {}의 위치 정보 저장을 성공적으로 완료했습니다.", protectedUserId);
    }

    /**
     * 피보호자의 현재 위치를 기반으로 지오펜스 상태(진입/이탈)를 확인하고,
     * 상태 변경 시 알림 발송 및 기록을 저장합니다.
     *
     * @param pUser  검사할 피보호자 엔티티.
     * @param newLat 새로운 위치의 위도.
     * @param newLon 새로운 위치의 경도.
     */
    private void checkGeofence(ProtectedUser pUser, double newLat, double newLon) {
        // 지오펜스가 설정되지 않았으면 검사하지 않고 종료합니다.
        if (pUser.getHomeLatitude() == null || pUser.getGeofenceRadius() == null) {
            return;
        }

        double distance = haversine(pUser.getHomeLatitude(), pUser.getHomeLongitude(), newLat, newLon);
        boolean wasInside = pUser.isInsideGeofence();
        boolean isNowInside = distance <= pUser.getGeofenceRadius();

        // 상태가 변경되었을 때만(안->밖 또는 밖->안) 알림/기록을 처리합니다.
        if (wasInside && !isNowInside) { // 안 -> 밖 (이탈)
            pUser.setInsideGeofence(false); // 현재 상태를 '외부'로 갱신
            log.warn("[지오펜스] 피보호자 ID: {}가 안심 구역을 벗어났습니다! (거리: {}m)", pUser.getId(), String.format("%.2f", distance));

            // 1. 이탈 기록을 DB에 저장합니다.
            alertLogRepository.save(AlertLog.builder()
                    .protectedUser(pUser)
                    .eventType(EventType.GEOFENCE_EXIT)
                    .message("안심 구역을 벗어났습니다.")
                    .eventTime(LocalDateTime.now())
                    .latitude(newLat).longitude(newLon)
                    .build());

            // 2. 모든 보호자에게 푸시 알림을 발송합니다.
            notifyGuardians(pUser, "🚨 안심구역 이탈!", "연결된 사용자가 설정된 안심 구역을 벗어났습니다.");

        } else if (!wasInside && isNowInside) { // 밖 -> 안 (진입)
            pUser.setInsideGeofence(true); // 현재 상태를 '내부'로 갱신
            log.info("[지오펜스] 피보호자 ID: {}가 안심 구역으로 돌아왔습니다.", pUser.getId());

            // 1. 진입 기록을 DB에 저장합니다.
            alertLogRepository.save(AlertLog.builder()
                    .protectedUser(pUser)
                    .eventType(EventType.GEOFENCE_ENTER)
                    .message("안심 구역으로 돌아왔습니다.")
                    .eventTime(LocalDateTime.now())
                    .latitude(newLat).longitude(newLon)
                    .build());

            // 2. 보호자에게 푸시 알림 발송 (현재는 주석 처리, 필요 시 활성화)
            // notifyGuardians(pUser, "안심구역 진입", "자녀가 안심 구역으로 돌아왔습니다.");
        }
    }

    /**
     * 특정 피보호자와 연결된 모든 보호자에게 FCM 푸시 알림을 발송하는 헬퍼 메소드.
     */
    private void notifyGuardians(ProtectedUser pUser, String title, String body) {
        List<Relationship> relationships = relationshipRepository.findAllByProtectedUser(pUser);
        for (Relationship rel : relationships) {
            User guardian = rel.getGuardian();
            fcmService.sendPushNotification(guardian.getFcmToken(), title, body);
        }
    }

    /**
     * 두 지점의 위도, 경도 좌표를 사용하여 거리를 계산하는 Haversine 공식 구현체.
     * @return 두 지점 간의 거리 (미터 단위).
     */
    private double haversine(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371 * 1000; // 지구 반지름 (미터)
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
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
