package com.Guard.Back.Dto;

import lombok.Builder;
import lombok.Getter;

/*로그인한 사용자의 상세 정보를 클라이언트에게 전달하기 위한 데이터 전송 객체(DTO).*/
@Getter
@Builder
public class UserInfoDto {
    /*사용자의 고유 ID.*/
    private Long userId;

    /*사용자의 역할 ("GUARDIAN" 또는 "PROTECTED").*/
    private String userType;

    /*사용자의 닉네임.*/
    private String nickname;

    /**
     * 보호자의 경우, 연결된 피보호자의 ID.
     * 피보호자이거나 연결된 피보호자가 없으면 null이 됩니다.
     */
    private Long linkedUserId;
}