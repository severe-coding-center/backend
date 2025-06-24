package com.Guard.Back.Auth;

import com.Guard.Back.Dto.OAuthUserInfoDto;

public interface OAuthService {
    OAuthUserInfoDto getUserInfo(String code, String state);
    OAuthProvider provider();
}