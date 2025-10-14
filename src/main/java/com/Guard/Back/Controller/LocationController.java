package com.Guard.Back.Controller;

import com.Guard.Back.Dto.LocationRequest;
import com.Guard.Back.Dto.LocationResponse;
import com.Guard.Back.Service.LocationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/location")
@RequiredArgsConstructor
public class LocationController {

    private final LocationService locationService;

    @PostMapping
    public ResponseEntity<Void> uploadLocation(@RequestBody LocationRequest request, Authentication authentication) {
        Long protectedUserId = Long.parseLong(authentication.getName());
        locationService.saveLocation(protectedUserId, request);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{protectedUserId}")
    public ResponseEntity<LocationResponse> getLatestLocation(@PathVariable Long protectedUserId, Authentication authentication) {
        Long currentGuardianId = Long.parseLong(authentication.getName());
        LocationResponse latestLocation = locationService.getLatestLocation(protectedUserId, currentGuardianId);

        if (latestLocation == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(latestLocation);
    }
}