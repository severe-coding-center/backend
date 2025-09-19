package com.Guard.Back.Dto;

/**
 * 피보호자 인증 관련 요청/응답을 위한 데이터 전송 객체(DTO)들을 포함합니다.
 */
public class ProtectedUserDto {
    /**
     * 피보호자 등록/로그인 요청 DTO.
     * @param deviceId 앱이 설치된 기기의 고유 ID
     */
    public record RegisterRequest(String deviceId) {}

    /**
     * 피보호자 등록/로그인 성공 시 클라이언트에 반환될 DTO.
     * @param accessToken API 접근 권한을 증명하는 단기 토큰
     * @param refreshToken AccessToken 재발급에 사용되는 장기 토큰
     * @param linkingCode 보호자와의 연동을 위한 일회성 코드
     */
    public record RegisterResponse(String accessToken, String refreshToken, String linkingCode) {}
}