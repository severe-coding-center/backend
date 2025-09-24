package com.Guard.Back.Dto;

/**
 * 보호자 인증 관련 요청/응답을 위한 데이터 전송 객체(DTO)들을 포함합니다.
 */
public class AuthDto {
    // --- 회원가입 1단계: 인증번호 요청 DTO ---
    public record PhoneRequest(String phoneNumber) {}

    // --- 회원가입 2단계: 인증번호 검증 DTO ---
    public record VerificationRequest(String phoneNumber, String code) {}

    // --- 회원가입 3단계: 최종 정보 등록 DTO ---
    public record SignUpRequest(String name, String phoneNumber, String password) {}

    /**
     * 보호자 로그인 요청 DTO.
     */
    public record LoginRequest(String phoneNumber, String password) {}

    /**
     * 인증 성공 시 클라이언트에 반환될 DTO.
     */
    public record AuthResponse(String accessToken, String refreshToken) {}

    // --- 💡 [수정] 토큰 재발급 요청 DTO ---
    public record RefreshRequest(String refreshToken) {}

    // --- 💡 [수정] 토큰 재발급 응답 DTO (새로운 Refresh Token 포함) ---
    public record RefreshResponse(String accessToken, String refreshToken) {}
}