package com.Guard.Back.Controller;

import com.Guard.Back.Dto.RelationshipDto.LinkRequest;
import com.Guard.Back.Service.RelationshipService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;

@RestController
@RequestMapping("/api/relationship")
@RequiredArgsConstructor
public class RelationshipController {
    private final RelationshipService relationshipService;

    @PostMapping("/link")
    public ResponseEntity<Void> link(@RequestBody LinkRequest request, Authentication authentication) {
        // 💡 [최종 변경] authentication.getName()을 통해 로그인한 사용자의 ID(String)를 가져옵니다.
        Long currentGuardianId = Long.parseLong(authentication.getName());

        relationshipService.createRelationship(request.linkingCode(), currentGuardianId);
        return ResponseEntity.ok().build();
    }
}