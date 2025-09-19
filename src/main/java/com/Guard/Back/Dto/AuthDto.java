package com.Guard.Back.Dto;

/**
 * 보호자 인증 관련 요청/응답을 위한 데이터 전송 객체(DTO)들을 포함합니다.
 */
public class AuthDto {
    /**
     * 보호자 회원가입 요청 DTO.
     * @param name 이름
     * @param phoneNumber 휴대폰 번호
     */
    public record SignUpRequest(String name, String phoneNumber) {}

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
}