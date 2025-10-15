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
}