package com.Guard.Back.Dto;

import com.Guard.Back.Domain.OAuthProvider;
import lombok.Builder;
import lombok.Getter;

/**
 * 다양한 소셜 로그인 제공자(OAuth Provider)로부터 받은 사용자 정보를
 * 우리 시스템에서 공통적으로 사용하기 위한 표준 데이터 전송 객체(DTO).
 */
@Getter
@Builder
public class OAuthUserInfoDto {
    /**
     * 소셜 로그인 제공자 타입 (e.g., KAKAO).
     */
    private OAuthProvider provider;

    /**
     * 해당 소셜 로그인 서비스 내에서 사용자를 식별하는 고유 ID.
     */
    private String providerId;

    /**
     * 사용자의 닉네임.
     */
    private String nickname;

    /**
     * 사용자의 프로필 이미지 URL.
     */
    private String profileImage;
}