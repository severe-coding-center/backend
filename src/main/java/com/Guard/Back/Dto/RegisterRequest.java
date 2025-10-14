package com.Guard.Back.Dto;

import jakarta.validation.constraints.NotBlank;

/*피보호자가 등록 또는 로그인을 요청할 때 사용하는 데이터 전송 객체(DTO).*/
public record RegisterRequest(
        /*
         * 피보호자의 앱이 설치된 기기의 고유 ID.
         * null이거나 비어있을 수 없습니다.
         */
        @NotBlank
        String deviceId
) {}