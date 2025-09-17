package com.Guard.Back.Controller;

import com.Guard.Back.Dto.RelationshipDto.LinkRequest;
import com.Guard.Back.Service.RelationshipService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/relationship")
@RequiredArgsConstructor
public class RelationshipController {
    private final RelationshipService relationshipService;

    @PostMapping("/link")
    public ResponseEntity<Void> link(@RequestBody LinkRequest request) {
        // 💡 [중요] 실제로는 JWT 토큰에서 보호자 ID를 가져와야 합니다.
        // 지금은 임시로 1L을 사용합니다.
        Long currentGuardianId = 3L;
        relationshipService.createRelationship(request.linkingCode(), currentGuardianId);
        return ResponseEntity.ok().build();
    }
}