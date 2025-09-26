package com.Guard.Back.Service;

import com.Guard.Back.Domain.OAuthProvider;
import com.Guard.Back.Dto.KakaoUserInfoResponseDto;
import com.Guard.Back.Dto.OAuthUserInfoDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@RequiredArgsConstructor
public class KakaoOAuthService implements OAuthService {

    private final WebClient webClient;

    @Override
    public OAuthProvider provider() {
        return OAuthProvider.KAKAO;
    }

    @Override
    public OAuthUserInfoDto getUserInfo(String accessToken) {
        KakaoUserInfoResponseDto userInfo = webClient.get()
                .uri("https://kapi.kakao.com/v2/user/me")
                .header("Authorization", "Bearer " + accessToken)
                .retrieve()
                .bodyToMono(KakaoUserInfoResponseDto.class)
                .block();

        return OAuthUserInfoDto.builder()
                .provider(OAuthProvider.KAKAO)
                .providerId(String.valueOf(userInfo.getId()))
                .nickname(userInfo.getProperties().get("nickname"))
                .profileImage(userInfo.getProperties().get("profile_image"))
                .build();
    }
}