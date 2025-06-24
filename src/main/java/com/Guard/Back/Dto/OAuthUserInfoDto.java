package com.Guard.Back.Dto;

import com.Guard.Back.Auth.OAuthProvider;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OAuthUserInfoDto {
    private String id;
    private String nickname;
    private String email;
    private String profileImage;
    private OAuthProvider provider;
}

