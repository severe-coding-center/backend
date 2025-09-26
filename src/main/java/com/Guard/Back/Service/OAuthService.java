package com.Guard.Back.Service;

import com.Guard.Back.Domain.OAuthProvider;
import com.Guard.Back.Dto.OAuthUserInfoDto;

public interface OAuthService {
    OAuthProvider provider();
    OAuthUserInfoDto getUserInfo(String accessToken);
}