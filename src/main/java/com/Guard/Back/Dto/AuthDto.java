package com.Guard.Back.Dto;

/**
 * 사용자 인증(Authentication) 관련 요청/응답을 위한 데이터 전송 객체(DTO)들을 포함합니다.
 */
public class AuthDto {

    /**
     * 로그인 또는 회원가입 성공 시 클라이언트에 반환될 DTO.
     * @param accessToken API 접근 권한을 증명하는 단기 토큰
     * @param refreshToken AccessToken 재발급에 사용되는 장기 토큰
     */
    public record AuthResponse(String accessToken, String refreshToken) {}

    /**
     * 토큰 재발급을 요청할 때 사용하는 DTO.
     * @param refreshToken 유효한 Refresh Token
     */
    public record RefreshRequest(String refreshToken) {}

    /**
     * 토큰 재발급 성공 시 클라이언트에 반환될 DTO.
     * @param accessToken 새로 발급된 Access Token
     * @param refreshToken 새로 발급된 Refresh Token (보안을 위해 Refresh Token도 갱신)
     */
    public record RefreshResponse(String accessToken, String refreshToken) {}
}