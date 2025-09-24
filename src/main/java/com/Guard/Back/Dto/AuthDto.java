package com.Guard.Back.Dto;

/**
 * 보호자 인증 관련 요청/응답을 위한 데이터 전송 객체(DTO)들을 포함합니다.
 */
public class AuthDto {
    // --- 💡 [추가] 회원가입 1단계: 인증번호 요청 DTO ---
    public record PhoneRequest(String phoneNumber) {}

    // --- 💡 [추가] 회원가입 2단계: 인증번호 검증 DTO ---
    public record VerificationRequest(String phoneNumber, String code) {}

    // --- 💡 [수정] 회원가입 3단계: 최종 정보 등록 DTO (비밀번호 포함) ---
    public record SignUpRequest(String name, String phoneNumber, String password) {}


    /**
     * 보호자 로그인 요청 DTO.
     * @param phoneNumber 휴대폰 번호
     * @param password 비밀번호
     */
    public record LoginRequest(String phoneNumber, String password) {}

    /**
     * 인증 성공 시 클라이언트에 반환될 DTO.
     * @param accessToken API 접근 권한을 증명하는 단기 토큰
     * @param refreshToken AccessToken 재발급에 사용되는 장기 토큰
     */
    public record AuthResponse(String accessToken, String refreshToken) {}

    // 토큰 재발급 요청 DTO
    public record RefreshRequest(String refreshToken) {}

    // 토큰 재발급 응답 DTO
    public record RefreshResponse(String accessToken) {}
}