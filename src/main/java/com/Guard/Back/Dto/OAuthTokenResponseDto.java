package com.Guard.Back.Dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter @NoArgsConstructor
public class OAuthTokenResponseDto {
    private String access_token;   // access token
    private String refresh_token;  // 일부 플랫폼 미제공
    private String token_type;
    private long   expires_in;
}
