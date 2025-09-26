package com.Guard.Back.Dto;

import com.Guard.Back.Domain.OAuthProvider;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class OAuthUserInfoDto {
    private OAuthProvider provider;
    private String providerId;
    private String nickname;
    private String profileImage;
}