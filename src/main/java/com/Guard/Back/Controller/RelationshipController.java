package com.Guard.Back.Controller;

import com.Guard.Back.Dto.RelationshipDto.LinkRequest;
import com.Guard.Back.Service.RelationshipService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
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

        String userType = (String) authentication.getCredentials();
        if (!"GUARDIAN".equals(userType)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Long currentGuardianId = Long.parseLong(authentication.getName());
        relationshipService.createRelationship(request.linkingCode(), currentGuardianId);

        return ResponseEntity.ok().build();
    }

    /**
     * 특정 관계를 해제하는 API.
     * @param relationshipId 해제할 관계의 ID
     * @param authentication 현재 로그인한 사용자의 정보
     * @return 성공 시 200 OK
     */
    @DeleteMapping("/{relationshipId}")
    public ResponseEntity<Void> unlink(@PathVariable Long relationshipId, Authentication authentication) {
        Long currentUserId = Long.parseLong(authentication.getName());
        String currentUserType = (String) authentication.getCredentials();

        relationshipService.deleteRelationship(relationshipId, currentUserId, currentUserType);
        return ResponseEntity.ok().build();
    }
}