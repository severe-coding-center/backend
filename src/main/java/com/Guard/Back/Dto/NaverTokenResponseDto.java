package com.Guard.Back.Dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class NaverTokenResponseDto {
    private String access_token; private String refresh_token; private String token_type; private String expires_in;
}
