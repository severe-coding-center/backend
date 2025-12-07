package com.Guard.Back.Domain;

/**
 * 지원하는 소셜 로그인 제공자를 정의하는 Enum.
 * 현재는 KAKAO만 지원합니다.
 */
public enum OAuthProvider {
    KAKAO,
    GOOGLE
}