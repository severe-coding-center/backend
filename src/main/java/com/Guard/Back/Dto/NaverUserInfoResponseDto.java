package com.Guard.Back.Dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class NaverUserInfoResponseDto {
    private NaverUser response;
    @Getter @NoArgsConstructor
    public static class NaverUser { private String id; private String nickname; private String email; private String profile_image; }
}
