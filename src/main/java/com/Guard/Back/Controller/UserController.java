package com.Guard.Back.Controller;

import com.Guard.Back.Service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * 회원 탈퇴 등 사용자 계정 관리 API 요청을 처리하는 컨트롤러.
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * 현재 로그인한 사용자의 계정을 탈퇴시키는 API.
     * @param authentication 현재 로그인한 사용자의 정보
     * @return 성공 메시지
     */
    @DeleteMapping("/me")
    public ResponseEntity<String> deleteAccount(Authentication authentication) {
        Long currentUserId = Long.parseLong(authentication.getName());
        String currentUserType = (String) authentication.getCredentials();

        if ("GUARDIAN".equals(currentUserType)) {
            userService.deleteGuardian(currentUserId);
        } else if ("PROTECTED".equals(currentUserType)) {
            userService.deleteProtectedUser(currentUserId);
        } else {
            return ResponseEntity.badRequest().body("알 수 없는 사용자 타입입니다.");
        }

        return ResponseEntity.ok("회원 탈퇴가 성공적으로 처리되었습니다.");
    }
}