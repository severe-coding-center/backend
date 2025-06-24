package com.Guard.Back.Auth;

import com.Guard.Back.Dto.KakaoTokenResponseDto;
import com.Guard.Back.Dto.KakaoUserInfoResponseDto;
import com.Guard.Back.Dto.OAuthUserInfoDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class KakaoOAuthService implements OAuthService {

    private final WebClient webClient;

    @Value("${kakao.client-id}")
    private String clientId;
    @Value("${kakao.redirect-uri}")
    private String redirectUri;

    @Override
    public OAuthProvider provider() { return OAuthProvider.KAKAO; }

    @Override
    public OAuthUserInfoDto getUserInfo(String code, String state) {
        // 1. 토큰 요청
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "authorization_code");
        form.add("client_id", clientId);
        form.add("redirect_uri", redirectUri);
        form.add("code", code);

        KakaoTokenResponseDto token = webClient.post()
                .uri("https://kauth.kakao.com/oauth/token")
                .body(BodyInserters.fromFormData(form))
                .retrieve()
                .bodyToMono(KakaoTokenResponseDto.class)
                .block();
        Objects.requireNonNull(token, "카카오 토큰 응답이 null");

        // 2. 유저 정보 요청
        KakaoUserInfoResponseDto ui = webClient.get()
                .uri("https://kapi.kakao.com/v2/user/me")
                .header("Authorization", "Bearer " + token.getAccess_token())
                .retrieve()
                .bodyToMono(KakaoUserInfoResponseDto.class)
                .block();
        Objects.requireNonNull(ui, "카카오 사용자 정보 응답 null");

        return OAuthUserInfoDto.builder()
                .provider(OAuthProvider.KAKAO)
                .id(String.valueOf(ui.getId()))
                .nickname(ui.getProperties().get("nickname"))
                .profileImage(ui.getProperties().get("profile_image"))
                .build();
    }
}