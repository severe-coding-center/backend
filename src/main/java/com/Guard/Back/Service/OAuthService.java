package com.Guard.Back.Service;

import com.Guard.Back.Domain.OAuthProvider;
import com.Guard.Back.Dto.OAuthUserInfoDto;

/**
 * 소셜 로그인 서비스의 표준 인터페이스.
 * 모든 소셜 로그인 서비스 구현체는 이 인터페이스를 따름
 */
public interface OAuthService {

    /**
     * 현재 서비스가 어떤 소셜 로그인 제공자인지 반환
     * @return OAuthProvider Enum (e.g., KAKAO)
     */
    OAuthProvider provider();

    /**
     * 소셜 로그인 제공자로부터 받은 인가 코드를 사용하여 사용자 정보를 조회
     * @param code 소셜 로그인 콜백 시 전달받은 인가 코드
     * @return 표준화된 사용자 정보 DTO
     */
    OAuthUserInfoDto getUserInfo(String code);
}