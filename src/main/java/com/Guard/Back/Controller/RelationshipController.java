package com.Guard.Back.Controller;

import com.Guard.Back.Dto.LinkRequest; // 💡 import 변경
import com.Guard.Back.Service.RelationshipService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/relationship")
@RequiredArgsConstructor
public class RelationshipController {

    private final RelationshipService relationshipService;

    @PostMapping("/link")
    public ResponseEntity<Void> link(@RequestBody LinkRequest request, Authentication authentication) { // 💡 타입 변경

        String userType = (String) authentication.getCredentials();
        if (!"GUARDIAN".equals(userType)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

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