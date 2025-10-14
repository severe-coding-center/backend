package com.Guard.Back.Controller;

import com.Guard.Back.Dto.LinkRequest;
import com.Guard.Back.Service.RelationshipService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j; // 💡 Slf4j 임포트 추가
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * 보호자와 피보호자 간의 관계 생성 및 삭제 API 요청을 처리하는 컨트롤러.
 */
@RestController
@RequestMapping("/api/relationship")
@RequiredArgsConstructor
@Slf4j // 💡 로깅을 위한 어노테이션 추가
public class RelationshipController {

    private final RelationshipService relationshipService;

    /**
     * 보호자가 피보호자와 관계를 맺습니다.
     * SecurityConfig에 의해 GUARDIAN 역할만 접근 가능합니다.
     *
     * @param request        요청 DTO. 피보호자의 유효한 연동 코드를 포함합니다.
     * @param authentication 현재 로그인한 보호자의 인증 정보.
     * @return 성공 시 200 OK.
     * @throws com.Guard.Back.Exception.CustomException 연동 코드가 유효하지 않거나 비즈니스 규칙 위반 시 발생.
     */
    @PostMapping("/link")
    public ResponseEntity<Void> link(@Valid @RequestBody LinkRequest request, Authentication authentication) {
        Long currentGuardianId = Long.parseLong(authentication.getName());
        log.info("[관계 생성] 보호자 ID: {}가 연동 코드 '{}'를 사용하여 관계 생성을 요청했습니다.",
                currentGuardianId, request.linkingCode());

        relationshipService.createRelationship(request.linkingCode(), currentGuardianId);

        log.info("[관계 생성] 보호자 ID: {}의 관계 생성이 성공적으로 완료되었습니다.", currentGuardianId);
        return ResponseEntity.ok().build();
    }

    /**
     * 특정 관계를 삭제(해제)합니다.
     * 관계에 포함된 보호자 또는 피보호자 본인만 삭제할 수 있습니다.
     *
     * @param relationshipId 삭제할 관계의 고유 ID.
     * @param authentication 현재 로그인한 사용자의 인증 정보.
     * @return 성공 시 200 OK.
     * @throws com.Guard.Back.Exception.CustomException 관계가 존재하지 않거나 삭제 권한이 없는 경우 발생.
     */
    @DeleteMapping("/{relationshipId}")
    public ResponseEntity<Void> unlink(@PathVariable Long relationshipId, Authentication authentication) {
        Long currentUserId = Long.parseLong(authentication.getName());
        // 💡 [수정] JwtTokenProvider 변경에 따라 getCredentials() 대신 getAuthorities()를 사용합니다.
        String currentUserRole = authentication.getAuthorities().stream()
                .findFirst()
                .map(auth -> auth.getAuthority().replace("ROLE_", "")) // "ROLE_" 접두사 제거
                .orElse(null);

        log.info("[관계 해제] 사용자 ID: {} (역할: {})가 관계 ID: {}의 해제를 요청했습니다.",
                currentUserId, currentUserRole, relationshipId);

        relationshipService.deleteRelationship(relationshipId, currentUserId, currentUserRole);

        log.info("[관계 해제] 관계 ID: {}의 해제가 성공적으로 완료되었습니다.", relationshipId);
        return ResponseEntity.ok().build();
    }
}