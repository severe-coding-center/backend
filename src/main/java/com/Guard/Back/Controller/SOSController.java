package com.Guard.Back.Controller;

import com.Guard.Back.Service.SOSService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * SOS 긴급 호출 관련 API 요청을 처리하는 컨트롤러.
 */
@RestController
@RequestMapping("/api/sos")
@RequiredArgsConstructor
@Slf4j
public class SOSController {

    private final SOSService sosService;

    /**
     * 피보호자가 SOS 버튼을 눌렀을 때 호출되는 API.
     * 연결된 모든 보호자에게 푸시 알림을 발송
     *
     * @param authentication 현재 SOS를 요청한 피보호자의 인증 정보.
     * @return 성공 메시지.
     */
    @PostMapping
    public ResponseEntity<String> sendSOS(Authentication authentication) {
        Long protectedUserId = Long.parseLong(authentication.getName());
        log.info("[SOS] 피보호자 ID: {}로부터 긴급 호출이 접수되었습니다.", protectedUserId);

        sosService.sendSOSToGuardians(protectedUserId);

        log.info("[SOS] 피보호자 ID: {}의 긴급 호출 메시지 발송이 완료되었습니다.", protectedUserId);
        return ResponseEntity.ok("연결된 보호자에게 긴급 호출을 보냈습니다.");
    }
}