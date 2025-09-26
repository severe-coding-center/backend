package com.Guard.Back.Service;

import com.Guard.Back.Domain.OAuthProvider;
import com.Guard.Back.Dto.KakaoTokenResponseDto; // DTO 추가
import com.Guard.Back.Dto.KakaoUserInfoResponseDto;
import com.Guard.Back.Dto.OAuthUserInfoDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value; // Value 어노테이션 임포트
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@RequiredArgsConstructor
public class KakaoOAuthService implements OAuthService {

    private final WebClient webClient;

    // 💡 [변경] application-API-KEY.properties에서 값을 주입받습니다.
    @Value("${kakao.client-id}")
    private String clientId;

    @Value("${kakao.redirect-uri}")
    private String redirectUri;

    @Override
    public OAuthProvider provider() {
        return OAuthProvider.KAKAO;
    }

    /**
     * 💡 [변경] 인증 코드를 사용하여 카카오로부터 사용자 정보를 가져옵니다.
     * @param code 카카오 로그인 성공 후 Redirect된 인증 코드
     * @return 사용자 정보 DTO
     */
    @Override
    public OAuthUserInfoDto getUserInfo(String code) {
        // 1. 코드를 사용하여 Access Token 요청
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

        // 2. 발급받은 Access Token으로 사용자 정보 요청
        String accessToken = tokenResponse.getAccess_token();
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

    // 기존의 getUserInfo(String accessToken) 메소드는 삭제합니다.
}