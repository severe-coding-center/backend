package com.Guard.Back.Controller;

import com.Guard.Back.Dto.LocationLogDto.*;
import com.Guard.Back.Service.LocationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/location")
@RequiredArgsConstructor
public class LocationController {

    private final LocationService locationService;

    /**
     * 피보호자가 자신의 위치를 업로드하는 API.
     * 반드시 '피보호자'로 로그인한 사용자만 호출할 수 있습니다.
     */
    @PostMapping
    public ResponseEntity<Void> uploadLocation(@RequestBody LocationRequest request, Authentication authentication) {
        if (!"PROTECTED".equals(authentication.getCredentials())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build(); // 보호자는 접근 불가
        }
        Long protectedUserId = Long.parseLong(authentication.getName());
        locationService.saveLocation(protectedUserId, request);
        return ResponseEntity.ok().build();
    }

    /**
     * 보호자가 특정 피보호자의 최신 위치를 조회하는 API.
     * 반드시 '보호자'로 로그인한 사용자만 호출할 수 있습니다.
     */
    @GetMapping("/{protectedUserId}")
    public ResponseEntity<LocationResponse> getLatestLocation(@PathVariable Long protectedUserId, Authentication authentication) {
        if (!"GUARDIAN".equals(authentication.getCredentials())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build(); // 피보호자는 접근 불가
        }

        LocationResponse latestLocation = locationService.getLatestLocation(protectedUserId);
        if (latestLocation == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(latestLocation);
    }
}