package com.Guard.Back.Service;

import com.Guard.Back.Domain.ProtectedUser;
import com.Guard.Back.Domain.Relationship;
import com.Guard.Back.Domain.User;
import com.Guard.Back.Exception.CustomException;
import com.Guard.Back.Exception.ErrorCode;
import com.Guard.Back.Repository.ProtectedUserRepository;
import com.Guard.Back.Repository.RelationshipRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

/**
 * SOS 긴급 호출 비즈니스 로직을 처리하는 서비스 클래스.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SOSService {

    private final ProtectedUserRepository protectedUserRepository; // 💡 피보호자를 찾기 위해 추가
    private final RelationshipRepository relationshipRepository;
    private final FCMService fcmService; // 💡 FCM 서비스 주입

    /**
     * 특정 피보호자와 연결된 모든 보호자에게 SOS 푸시 알림을 발송합니다.
     * @param protectedUserId SOS를 요청한 피보호자의 ID.
     */
    @Transactional(readOnly = true)
    public void sendSOSToGuardians(Long protectedUserId) {
        log.info("[SOS] 피보호자 ID: {}와 연결된 모든 보호자에게 푸시 알림 발송을 시작합니다.", protectedUserId);
        ProtectedUser protectedUser = protectedUserRepository.findById(protectedUserId)
                .orElseThrow(() -> new CustomException(ErrorCode.PROTECTED_USER_NOT_FOUND));

        List<Relationship> relationships = relationshipRepository.findAllByProtectedUser(protectedUser);

        if (relationships.isEmpty()) {
            log.warn("[SOS] 피보호자 ID: {}는 연결된 보호자가 없어 메시지를 발송할 수 없습니다.", protectedUserId);
            return; // 연결된 보호자가 없으면 조용히 종료
        }

        for (Relationship relationship : relationships) {
            User guardian = relationship.getGuardian();
            log.info("[SOS] 보호자 ID: {}에게 푸시 알림 발송을 시도합니다.", guardian.getId());
            fcmService.sendPushNotification(
                    guardian.getFcmToken(),
                    "🚨 긴급 상황 발생!",
                    "SOS 호출이 있었습니다. 앱을 확인해주세요."
            );
        }
    }
}