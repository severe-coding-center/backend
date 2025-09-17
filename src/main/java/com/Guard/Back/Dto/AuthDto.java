package com.Guard.Back.Dto;

public class AuthDto {
    // 인증번호 요청 DTO
    public record LoginRequest(String phoneNumber) {}
    // 인증번호 검증 DTO
    public record VerifyRequest(String phoneNumber, String code) {}
    // 최종 응답 DTO
    public record AuthResponse(String accessToken) {}
}