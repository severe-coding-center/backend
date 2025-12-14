package com.Guard.Back.Controller;

import com.Guard.Back.Dto.LocationRequest;
import com.Guard.Back.Dto.LocationResponse;
import com.Guard.Back.Service.LocationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/*피보호자의 위치 정보 업로드 및 보호자의 위치 조회 API 요청을 처리하는 컨트롤러.*/
@RestController
@RequestMapping("/api/location")
@RequiredArgsConstructor
@Slf4j
public class LocationController {

    private final LocationService locationService;

    /**
     * 피보호자가 자신의 현재 위치 정보를 서버에 업로드(저장)
     * SecurityConfig에 의해 PROTECTED 역할만 접근
     *
     * @param request        요청 DTO. 위도, 경도, 기록 시간 정보를 포함
     * @param authentication 현재 로그인한 피보호자의 인증 정보.
     * @return 성공 시 200 OK.
     */
    @PostMapping
    public ResponseEntity<Void> uploadLocation(@RequestBody LocationRequest request, Authentication authentication) {
        Long protectedUserId = Long.parseLong(authentication.getName());
        log.info("[위치 업로드] 피보호자 ID: {}가 위치 정보 업로드를 요청했습니다. (위도: {}, 경도: {})",
                protectedUserId, request.latitude(), request.longitude());

        locationService.saveLocation(protectedUserId, request);

        log.info("[위치 업로드] 피보호자 ID: {}의 위치 정보가 성공적으로 저장되었습니다.", protectedUserId);
        return ResponseEntity.ok().build();
    }

    /**
     * 보호자가 자신과 연결된 특정 피보호자의 가장 최신 위치 정보를 조회
     * SecurityConfig에 의해 GUARDIAN 역할만 접근
     *
     * @param protectedUserId 조회하려는 피보호자의 ID.
     * @param authentication  현재 로그인한 보호자의 인증 정보.
     * @return 성공 시 최신 위치 정보가 담긴 DTO, 위치 기록이 없으면 404 Not Found.
     * @throws com.Guard.Back.Exception.CustomException 조회 권한이 없는 경우 발생.
     */
    @GetMapping("/{protectedUserId}")
    public ResponseEntity<LocationResponse> getLatestLocation(@PathVariable Long protectedUserId, Authentication authentication) {
        Long currentGuardianId = Long.parseLong(authentication.getName());
        log.info("[위치 조회] 보호자 ID: {}가 피보호자 ID: {}의 최신 위치 조회를 요청했습니다.",
                currentGuardianId, protectedUserId);

        LocationResponse latestLocation = locationService.getLatestLocation(protectedUserId, currentGuardianId);

        if (latestLocation == null) {
            log.warn("[위치 조회] 피보호자 ID: {}에 대한 위치 기록이 존재하지 않아 404 Not Found를 반환합니다.", protectedUserId);
            return ResponseEntity.notFound().build();
        }

        log.info("[위치 조회] 보호자 ID: {}가 요청한 피보호자 ID: {}의 위치 조회가 성공적으로 완료되었습니다.",
                currentGuardianId, protectedUserId);
        return ResponseEntity.ok(latestLocation);
    }
}