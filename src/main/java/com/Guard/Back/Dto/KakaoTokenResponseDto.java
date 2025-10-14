package com.Guard.Back.Dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 카카오 서버에 인가 코드를 보내고 Access Token을 요청했을 때,
 * 그 응답을 매핑하기 위한 데이터 전송 객체(DTO)입니다.
 * <p>
 * 카카오 API의 응답 필드명(snake_case)과 일치시켜 JSON을 자동으로 파싱합니다.
 */
@Getter
@NoArgsConstructor
public class KakaoTokenResponseDto {
    /**
     * 사용자 인증에 사용되는 Access Token.
     */
    private String access_token;

    /**
     * 토큰 타입. 보통 "bearer"로 고정됩니다.
     */
    private String token_type;

    /**
     * Access Token 갱신에 사용되는 Refresh Token.
     */
    private String refresh_token;

    /**
     * Access Token의 만료 시간 (초 단위).
     */
    private int expires_in;

    /**
     * 인증된 사용자의 정보 조회 권한 범위.
     */
    private String scope;

    /**
     * Refresh Token의 만료 시간 (초 단위).
     */
    private int refresh_token_expires_in;
}