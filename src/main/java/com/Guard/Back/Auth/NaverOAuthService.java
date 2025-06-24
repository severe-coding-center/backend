package com.Guard.Back.Auth;

import com.Guard.Back.Dto.NaverTokenResponseDto;
import com.Guard.Back.Dto.NaverUserInfoResponseDto;
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
public class NaverOAuthService implements OAuthService {

    private final WebClient webClient;

    @Value("${naver.client-id}") private String clientId;
    @Value("${naver.client-secret}") private String clientSecret;
    @Value("${naver.redirect-uri}") private String redirectUri;

    @Override
    public OAuthProvider provider() { return OAuthProvider.NAVER; }

    @Override
    public OAuthUserInfoDto getUserInfo(String code, String state) {
        // 1. 토큰 요청
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "authorization_code");
        form.add("client_id", clientId);
        form.add("client_secret", clientSecret);
        form.add("code", code);
        form.add("state", state);

        NaverTokenResponseDto token = webClient.post()
                .uri("https://nid.naver.com/oauth2.0/token")
                .body(BodyInserters.fromFormData(form))
                .retrieve()
                .bodyToMono(NaverTokenResponseDto.class)
                .block();
        Objects.requireNonNull(token, "네이버 토큰 응답 null");

        // 2. 유저 정보 요청
        NaverUserInfoResponseDto ui = webClient.get()
                .uri("https://openapi.naver.com/v1/nid/me")
                .header("Authorization", "Bearer " + token.getAccess_token())
                .retrieve()
                .bodyToMono(NaverUserInfoResponseDto.class)
                .block();
        Objects.requireNonNull(ui, "네이버 사용자 정보 응답 null");

        return OAuthUserInfoDto.builder()
                .provider(OAuthProvider.NAVER)
                .id(ui.getResponse().getId())
                .nickname(ui.getResponse().getNickname())
                .profileImage(ui.getResponse().getProfile_image())
                .build();
    }
}