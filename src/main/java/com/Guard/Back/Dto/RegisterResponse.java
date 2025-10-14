package com.Guard.Back.Dto;

/*피보호자의 등록 또는 로그인 성공 시 클라이언트에 반환될 데이터 전송 객체(DTO).*/
public record RegisterResponse(
        /* API 접근 권한을 증명하는 단기 토큰 (Access Token).*/
        String accessToken,

        /*Access Token 재발급에 사용되는 장기 토큰 (Refresh Token).*/
        String refreshToken,

        /* 보호자와의 연동을 위한 일회성 6자리 코드.*/
        String linkingCode
) {}