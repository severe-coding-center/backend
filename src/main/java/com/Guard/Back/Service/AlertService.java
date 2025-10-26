package com.Guard.Back.Service;

import com.Guard.Back.Domain.AlertLog;
import com.Guard.Back.Domain.ProtectedUser;
import com.Guard.Back.Domain.User;
import com.Guard.Back.Dto.AlertLogDto;
import com.Guard.Back.Exception.CustomException;
import com.Guard.Back.Exception.ErrorCode;
import com.Guard.Back.Repository.AlertLogRepository;
import com.Guard.Back.Repository.ProtectedUserRepository;
import com.Guard.Back.Repository.RelationshipRepository;
import com.Guard.Back.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

/**
 * SOS, 지오펜스 등 주요 이벤트 기록을 조회하는 비즈니스 로직을 처리하는 서비스.
 */
@Service
@RequiredArgsConstructor
public class AlertService {
    private final AlertLogRepository alertLogRepository;
    private final ProtectedUserRepository protectedUserRepository;
    private final UserRepository userRepository;
    private final RelationshipRepository relationshipRepository;

    /**
     * 특정 피보호자의 모든 알림 기록을 최신순으로 조회합니다.
     * 요청한 보호자가 해당 피보호자와 관계를 맺고 있는지 반드시 확인합니다.
     *
     * @param protectedUserId 기록을 조회할 피보호자의 ID.
     * @param guardianId      요청을 보낸 보호자의 ID (권한 검증용).
     * @return 알림 기록 DTO 리스트.
     * @throws CustomException 보호자/피보호자가 존재하지 않거나, 두 사용자 간의 관계가 없을 경우 발생.
     */
    @Transactional(readOnly = true)
    public List<AlertLogDto> getAlertLogs(Long protectedUserId, Long guardianId) { // 💡 guardianId 파라미터 추가
        // 1. 요청자와 대상 사용자가 DB에 존재하는지 확인
        User guardian = userRepository.findById(guardianId)
                .orElseThrow(() -> new CustomException(ErrorCode.GUARDIAN_NOT_FOUND));
        ProtectedUser pUser = protectedUserRepository.findById(protectedUserId)
                .orElseThrow(() -> new CustomException(ErrorCode.PROTECTED_USER_NOT_FOUND));

        // 2. 두 사용자 간의 관계가 유효한지 확인 (보안 검증)
        if (!relationshipRepository.existsByGuardianAndProtectedUser(guardian, pUser)) {
            throw new CustomException(ErrorCode.UNAUTHORIZED_ACCESS);
        }

        // 3. 권한이 확인되면, 피보호자의 모든 알림 기록을 조회하여 반환
        return alertLogRepository.findAllByProtectedUserOrderByEventTimeDesc(pUser)
                .stream()
                .map(log -> new AlertLogDto(log.getEventType(), log.getMessage(), log.getEventTime()))
                .collect(Collectors.toList());
    }
}
