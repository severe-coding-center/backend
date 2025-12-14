package com.Guard.Back.Dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.Map;

/**
 * 카카오 서버에 Access Token을 보내고 사용자 정보를 요청했을 때,
 * 그 응답을 매핑하기 위한 데이터 전송 객체(DTO)
 * <p>
 * @see <a href="https://developers.kakao.com/docs/latest/ko/kakaologin/rest-api#req-user-info">카카오 사용자 정보 가져오기 API 명세</a>
 */
@Getter
@NoArgsConstructor
public class KakaoUserInfoResponseDto {
    /*카카오가 발급한 사용자의 고유 ID.*/
    private Long id;

    /**
     * 사용자의 추가 정보를 담고 있는 객체.
     * 닉네임(nickname), 프로필 이미지(profile_image) 등의 정보가 포함
     */
    private Map<String, String> properties;
}