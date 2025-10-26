package com.Guard.Back.Service;

import com.Guard.Back.Domain.ProtectedUser;
import com.Guard.Back.Repository.ProtectedUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j; // 💡 Slf4j 임포트 추가
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;

/*피보호자(ProtectedUser)의 등록 및 로그인 비즈니스 로직을 처리하는 서비스 클래스.*/
@Service
@RequiredArgsConstructor
@Slf4j
public class ProtectedUserService {

    private final ProtectedUserRepository protectedUserRepository;

    /**
     * 기기 ID를 기반으로 피보호자를 등록하거나 로그인 처리합니다.
     * DB에 기기 ID가 존재하면 기존 사용자를 반환하고(로그인),
     * 존재하지 않으면 새로운 사용자를 생성하여 저장합니다(등록).
     *
     * @param deviceId 앱에서 전달받은 기기 고유 ID
     * @return 기존 사용자 또는 새로 생성된 사용자 엔티티
     */
    @Transactional
    public ProtectedUser registerOrLogin(String deviceId) {
        log.info("[피보호자 등록/로그인] 기기 ID: {} 로 사용자 조회를 시작합니다.", deviceId);
        return protectedUserRepository.findByDeviceId(deviceId)
                .orElseGet(() -> {
                    log.info("[피보호자 등록/로그인] 기기 ID: {}에 해당하는 사용자가 없어 새로 등록합니다.", deviceId);
                    String linkingCode = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
                    ProtectedUser newUser = ProtectedUser.builder()
                            .deviceId(deviceId)
                            .linkingCode(linkingCode)
                            .build();
                    protectedUserRepository.save(newUser);
                    log.info("[피보호자 등록/로그인] 새로운 피보호자(ID: {})가 성공적으로 등록되었습니다.", newUser.getId());
                    return newUser;
                });
    }
}