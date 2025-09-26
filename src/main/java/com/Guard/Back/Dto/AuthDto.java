package com.Guard.Back.Dto;

public class AuthDto {
    // 인증 성공 시 클라이언트에 반환될 DTO.
    public record AuthResponse(String accessToken, String refreshToken) {}

    // 토큰 재발급 요청 DTO
    public record RefreshRequest(String refreshToken) {}

    // 토큰 재발급 응답 DTO
    public record RefreshResponse(String accessToken, String refreshToken) {}
}