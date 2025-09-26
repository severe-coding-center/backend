package com.Guard.Back.Service;

import com.Guard.Back.Domain.OAuthProvider;
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
import java.util.Objects; // 💡 Objects 임포트 추가

@Service
@RequiredArgsConstructor
public class KakaoOAuthService implements OAuthService {

    private final WebClient webClient;

    @Value("${kakao.client-id}")
    private String clientId;

    @Value("${kakao.redirect-uri}")
    private String redirectUri;

    @Override
    public OAuthProvider provider() {
        return OAuthProvider.KAKAO;
    }

    @Override
    public OAuthUserInfoDto getUserInfo(String code) {
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("grant_type", "authorization_code");
        formData.add("client_id", clientId);
        formData.add("redirect_uri", redirectUri);
        formData.add("code", code);

        KakaoTokenResponseDto tokenResponse = webClient.post()
                .uri("https://kauth.kakao.com/oauth/token")
                .body(BodyInserters.fromFormData(formData))
                .retrieve()
                .bodyToMono(KakaoTokenResponseDto.class)
                .block();

        // 💡 [수정] 토큰 응답이 null일 경우를 대비한 방어 코드 추가
        Objects.requireNonNull(tokenResponse, "카카오 토큰 응답이 null입니다.");
        String accessToken = tokenResponse.getAccess_token();

        KakaoUserInfoResponseDto userInfo = webClient.get()
                .uri("https://kapi.kakao.com/v2/user/me")
                .header("Authorization", "Bearer " + accessToken)
                .retrieve()
                .bodyToMono(KakaoUserInfoResponseDto.class)
                .block();

        // 💡 [수정] 사용자 정보 응답이 null일 경우를 대비한 방어 코드 추가
        Objects.requireNonNull(userInfo, "카카오 사용자 정보 응답이 null입니다.");

        return OAuthUserInfoDto.builder()
                .provider(OAuthProvider.KAKAO)
                .providerId(String.valueOf(userInfo.getId()))
                .nickname(userInfo.getProperties().get("nickname"))
                .profileImage(userInfo.getProperties().get("profile_image"))
                .build();
    }
}