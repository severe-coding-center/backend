// 1️⃣ OAuthProvider.java – 로그인 제공자 식별용 열거형
package com.Guard.Back.Auth;

/**
 * 지원하는 OAuth 제공자 목록.
 * 확장 시 enum 값만 추가하면 끝납니다.
 */
public enum OAuthProvider {
    KAKAO,
    NAVER,
}