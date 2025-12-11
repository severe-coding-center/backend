package com.Guard.Back.Service;

import com.Guard.Back.Domain.OAuthProvider;
import com.Guard.Back.Dto.GoogleTokenResponseDto;
import com.Guard.Back.Dto.GoogleUserInfoResponseDto;
import com.Guard.Back.Dto.OAuthUserInfoDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
@Slf4j
public class GoogleOAuthService implements OAuthService {

    private final WebClient webClient;

    @Value("${google.client-id}")
    private String clientId;
    @Value("${google.client-secret}")
    private String clientSecret;
    @Value("${google.redirect-uri}")
    private String redirectUri;

    @Override
    public OAuthProvider provider() { return OAuthProvider.GOOGLE; }

    @Override
    public OAuthUserInfoDto getUserInfo(String code) {
        log.info("[구글 로그인] 토큰 요청 시작");
        String decodedCode = URLDecoder.decode(code, StandardCharsets.UTF_8);

        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("code", decodedCode);
        formData.add("client_id", clientId);
        formData.add("client_secret", clientSecret);
        formData.add("redirect_uri", redirectUri);
        formData.add("grant_type", "authorization_code");

        GoogleTokenResponseDto tokenResponse = webClient.post()
                .uri("https://oauth2.googleapis.com/token")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData(formData))
                .retrieve().bodyToMono(GoogleTokenResponseDto.class).block();

        GoogleUserInfoResponseDto userInfo = webClient.get()
                .uri("https://www.googleapis.com/oauth2/v2/userinfo")
                .header("Authorization", "Bearer " + tokenResponse.getAccessToken())
                .retrieve().bodyToMono(GoogleUserInfoResponseDto.class).block();

        return OAuthUserInfoDto.builder()
                .provider(OAuthProvider.GOOGLE)
                .providerId(userInfo.getId())
                .nickname(userInfo.getName())
                .email(userInfo.getEmail()) // 이메일!
                .profileImage(userInfo.getPicture())
                .build();
    }
}