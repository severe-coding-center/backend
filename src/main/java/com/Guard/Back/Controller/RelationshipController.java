package com.Guard.Back.Controller;

import com.Guard.Back.Dto.LinkRequest; // 💡 import 변경
import com.Guard.Back.Service.RelationshipService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/relationship")
@RequiredArgsConstructor
public class RelationshipController {

    private final RelationshipService relationshipService;

    @PostMapping("/link")
    public ResponseEntity<Void> link(@Valid @RequestBody LinkRequest request, Authentication authentication) {
        // 💡 [삭제] 역할 확인 로직을 SecurityConfig에 위임하고 삭제합니다.
        // String userType = (String) authentication.getCredentials();
        // if (!"GUARDIAN".equals(userType)) { ... }

        Long currentGuardianId = Long.parseLong(authentication.getName());
        relationshipService.createRelationship(request.linkingCode(), currentGuardianId);

        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{relationshipId}")
    public ResponseEntity<Void> unlink(@PathVariable Long relationshipId, Authentication authentication) {
        Long currentUserId = Long.parseLong(authentication.getName());
        String currentUserType = (String) authentication.getCredentials();

        relationshipService.deleteRelationship(relationshipId, currentUserId, currentUserType);
        return ResponseEntity.ok().build();
    }
}