package com.Guard.Back.Dto;

import jakarta.validation.constraints.NotBlank;

/*보호자가 피보호자와의 관계 생성을 요청할 때 사용하는 데이터 전송 객체(DTO).*/
public record LinkRequest(
        /*
         * 피보호자의 앱 화면에 표시되는 6자리 연동 코드.
         * null이거나 비어있을 수 없습니다.
         */
        @NotBlank
        String linkingCode
) {}