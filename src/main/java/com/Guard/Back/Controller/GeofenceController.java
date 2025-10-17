package com.Guard.Back.Controller;

import com.Guard.Back.Dto.GeofenceDto;
import com.Guard.Back.Service.GeofenceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/geofence")
@RequiredArgsConstructor
public class GeofenceController {

    private final GeofenceService geofenceService;

    @PostMapping("/{protectedUserId}")
    public ResponseEntity<Void> setGeofence(
            @PathVariable Long protectedUserId,
            @Valid @RequestBody GeofenceDto geofenceDto,
            Authentication authentication) {
        Long guardianId = Long.parseLong(authentication.getName());
        geofenceService.setGeofence(guardianId, protectedUserId, geofenceDto);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{protectedUserId}")
    public ResponseEntity<Void> clearGeofence(
            @PathVariable Long protectedUserId,
            Authentication authentication) {
        Long guardianId = Long.parseLong(authentication.getName());
        geofenceService.clearGeofence(guardianId, protectedUserId);
        return ResponseEntity.ok().build();
    }

    // 맵스크린 다시 켰을 때 기존 안전반경 불러오는 API
    @GetMapping("/{protectedUserId}")
    public ResponseEntity<GeofenceDto> getGeofence(
            @PathVariable Long protectedUserId,
            Authentication authentication) {
        Long guardianId = Long.parseLong(authentication.getName());
        GeofenceDto geofenceDto = geofenceService.getGeofence(guardianId, protectedUserId);

        // 서비스에서 null을 반환하면(설정된 값이 없으면) 404 Not Found 응답
        if (geofenceDto == null) {
            return ResponseEntity.notFound().build();
        }

        // 설정된 값이 있으면 200 OK 와 함께 데이터 반환
        return ResponseEntity.ok(geofenceDto);
    }
}