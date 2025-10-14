package com.Guard.Back.Controller;

import com.Guard.Back.Domain.UserRole;
import com.Guard.Back.Service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import com.Guard.Back.Dto.UserInfoDto;
import org.springframework.web.bind.annotation.GetMapping;

/*회원 탈퇴 등 사용자 계정 관리 API 요청을 처리하는 컨트롤러.*/
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j // 💡 로깅을 위한 어노테이션 추가
public class UserController {

    private final UserService userService;

    /**
     * 현재 로그인한 사용자의 정보를 조회하는 API.
     *
     * @param authentication 현재 로그인한 사용자의 인증 정보.
     * @return 성공 시 사용자의 상세 정보가 담긴 DTO.
     */
    @GetMapping("/me")
    public ResponseEntity<UserInfoDto> getUserInfo(Authentication authentication) {
        Long currentUserId = Long.parseLong(authentication.getName());

        String currentUserType = authentication.getAuthorities().stream()
                .findFirst()
                .map(auth -> auth.getAuthority().replace("ROLE_", "")) // "ROLE_" 접두사 제거
                .orElse(null);

        log.info("[사용자 정보 조회] 사용자 ID: {}, 역할: {}의 정보 조회를 요청했습니다.", currentUserId, currentUserType);
        UserInfoDto userInfo = userService.getUserInfo(currentUserId, currentUserType);
        log.info("[사용자 정보 조회] 사용자 ID: {}의 정보 조회가 완료되었습니다.", currentUserId);
        return ResponseEntity.ok(userInfo);
    }

    /**
     * 현재 로그인한 사용자의 계정을 탈퇴시킵니다.
     * 연관된 모든 관계, 리프레시 토큰 등이 함께 삭제됩니다.
     *
     * @param authentication 현재 로그인한 사용자의 인증 정보.
     * @return 성공 메시지.
     */
    @DeleteMapping("/me")
    public ResponseEntity<String> deleteAccount(Authentication authentication) {
        Long currentUserId = Long.parseLong(authentication.getName());

        // 💡 [수정] JwtTokenProvider 변경에 따라 getCredentials() 대신 getAuthorities()를 사용합니다.
        String currentUserRoleKey = authentication.getAuthorities().stream()
                .findFirst()
                .map(auth -> auth.getAuthority()) // "ROLE_GUARDIAN"
                .orElse(null);

        log.info("[회원 탈퇴] 사용자 ID: {} (역할: {})가 계정 삭제를 요청했습니다.", currentUserId, currentUserRoleKey);

        if (UserRole.GUARDIAN.getKey().equals(currentUserRoleKey)) {
            userService.deleteGuardian(currentUserId);
        } else if (UserRole.PROTECTED.getKey().equals(currentUserRoleKey)) {
            userService.deleteProtectedUser(currentUserId);
        } else {
            log.error("[회원 탈퇴] 알 수 없는 사용자 역할({})로 인해 탈퇴 처리에 실패했습니다.", currentUserRoleKey);
            return ResponseEntity.badRequest().body("알 수 없는 사용자 타입입니다.");
        }

        log.info("[회원 탈퇴] 사용자 ID: {}의 계정 삭제가 성공적으로 완료되었습니다.", currentUserId);
        return ResponseEntity.ok("회원 탈퇴가 성공적으로 처리되었습니다.");
    }
}