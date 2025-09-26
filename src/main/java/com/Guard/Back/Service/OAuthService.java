package com.Guard.Back.Service;

import com.Guard.Back.Domain.OAuthProvider;
import com.Guard.Back.Dto.OAuthUserInfoDto;

public interface OAuthService {
    OAuthProvider provider();
    // 💡 [변경] 파라미터를 code로 변경
    OAuthUserInfoDto getUserInfo(String code);
}