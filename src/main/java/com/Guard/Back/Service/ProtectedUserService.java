package com.Guard.Back.Service;

import com.Guard.Back.Domain.ProtectedUser;
import com.Guard.Back.Repository.ProtectedUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;

/**
 * 피보호자(ProtectedUser)의 등록 및 로그인 비즈니스 로직을 처리하는 서비스 클래스.
 */
@Service
@RequiredArgsConstructor
public class ProtectedUserService {

    private final ProtectedUserRepository protectedUserRepository;

    /**
     * 기기 ID를 기반으로 피보호자를 등록하거나 로그인 처리합니다.
     * @param deviceId 앱에서 전달받은 기기 고유 ID
     * @return 기존 사용자 또는 새로 생성된 사용자 엔티티
     */
    @Transactional
    public ProtectedUser registerOrLogin(String deviceId) {
        // 기기 ID로 사용자를 조회하여, 존재하면 해당 유저를 반환하고, 없으면 새로 생성하여 반환(orElseGet)
        return protectedUserRepository.findByDeviceId(deviceId)
                .orElseGet(() -> {
                    String linkingCode = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
                    ProtectedUser newUser = ProtectedUser.builder()
                            .deviceId(deviceId)
                            .linkingCode(linkingCode)
                            .build();
                    return protectedUserRepository.save(newUser);
                });
    }
}