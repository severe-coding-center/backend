package com.Guard.Back.Controller;

import com.Guard.Back.Dto.AlertLogDto;
import com.Guard.Back.Service.AlertService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication; // 💡 import 추가
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * SOS, 지오펜스 등 주요 이벤트 기록 조회 API 요청을 처리하는 컨트롤러.
 */
@RestController
@RequestMapping("/api/alerts")
@RequiredArgsConstructor
public class AlertController {

    private final AlertService alertService;

    /**
     * 특정 피보호자의 모든 알림 기록을 최신순으로 조회합니다.
     * SecurityConfig에 의해 GUARDIAN 역할만 접근 가능합니다.
     *
     * @param protectedUserId 기록을 조회할 피보호자의 ID.
     * @param authentication  현재 로그인한 보호자의 인증 정보.
     * @return 성공 시 알림 기록 DTO 리스트.
     */
    @GetMapping("/{protectedUserId}")
    public ResponseEntity<List<AlertLogDto>> getAlertLogs(
            @PathVariable Long protectedUserId,
            Authentication authentication) { // 💡 Authentication 파라미터 추가

        // 💡 [수정] 현재 로그인한 보호자 ID를 인증 정보에서 추출합니다.
        Long guardianId = Long.parseLong(authentication.getName());

        // 💡 [수정] 서비스 호출 시 두 개의 인자(protectedUserId, guardianId)를 모두 전달합니다.
        List<AlertLogDto> logs = alertService.getAlertLogs(protectedUserId, guardianId);
        return ResponseEntity.ok(logs);
    }
}