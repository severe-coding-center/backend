package com.Guard.Back.Controller;

import com.Guard.Back.Dto.RelationshipDto.LinkRequest;
import com.Guard.Back.Service.RelationshipService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * 보호자와 피보호자 간의 관계 설정 API 요청을 처리하는 컨트롤러.
 */
@RestController
@RequestMapping("/api/relationship")
@RequiredArgsConstructor
public class RelationshipController {

    private final RelationshipService relationshipService;

    /**
     * 보호자가 피보호자의 연동 코드를 사용하여 연결을 요청하는 API.
     * 이 API는 반드시 보호자로 로그인한 사용자만 호출할 수 있습니다.
     * @param request 피보호자의 연동 코드
     * @param authentication JwtAuthenticationFilter가 주입해주는 현재 로그인한 사용자의 정보
     * @return 성공 시 200 OK
     */
    @PostMapping("/link")
    public ResponseEntity<Void> link(@RequestBody LinkRequest request, Authentication authentication) {

        // TODO: authentication.getCredentials() (userType)를 확인하여 "GUARDIAN" 타입의 사용자만 이 API를 호출할 수 있도록 검증 로직 추가

        // 1. Authentication 객체에서 현재 로그인한 보호자의 ID를 추출합니다.
        Long currentGuardianId = Long.parseLong(authentication.getName());

        // 2. RelationshipService를 통해 연동 로직을 수행합니다.
        relationshipService.createRelationship(request.linkingCode(), currentGuardianId);

        return ResponseEntity.ok().build();
    }
}