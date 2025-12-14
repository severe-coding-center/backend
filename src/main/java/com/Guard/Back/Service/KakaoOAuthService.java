package com.Guard.Back.Service;

import com.Guard.Back.Domain.OAuthProvider;
import com.Guard.Back.Dto.KakaoTokenResponseDto;
import com.Guard.Back.Dto.KakaoUserInfoResponseDto;
import com.Guard.Back.Dto.OAuthUserInfoDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j; // 💡 Slf4j 임포트 추가
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import java.util.Objects;

/*카카오 소셜 로그인을 처리하는 서비스 구현체.*/
@Service
@RequiredArgsConstructor
@Slf4j
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

    /**
     * 카카오 서버로부터 인가 코드를 받아 사용자 정보를 조회
     * 1. 인가 코드로 Access Token 요청
     * 2. Access Token으로 사용자 정보 요청
     *
     * @param code 카카오로부터 받은 인가 코드
     * @return 표준화된 사용자 정보 DTO
     */
    @Override
    public OAuthUserInfoDto getUserInfo(String code) {
        log.info("[카카오 로그인] 인가 코드를 사용하여 카카오 토큰 요청을 시작합니다.");
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("grant_type", "authorization_code");
        formData.add("client_id", clientId);
        formData.add("redirect_uri", redirectUri);
        formData.add("code", code);

        // 1. 카카오 서버에 Access Token 요청
        KakaoTokenResponseDto tokenResponse = webClient.post()
                .uri("https://kauth.kakao.com/oauth/token")
                .body(BodyInserters.fromFormData(formData))
                .retrieve()
                .bodyToMono(KakaoTokenResponseDto.class)
                .block();

        Objects.requireNonNull(tokenResponse, "카카오 토큰 응답이 null입니다.");
        String accessToken = tokenResponse.getAccess_token();
        log.info("[카카오 로그인] 카카오 Access Token을 성공적으로 발급받았습니다.");

        // 2. Access Token을 사용하여 사용자 정보 요청
        log.info("[카카오 로그인] Access Token을 사용하여 카카오 사용자 정보 요청을 시작합니다.");
        KakaoUserInfoResponseDto userInfo = webClient.get()
                .uri("https://kapi.kakao.com/v2/user/me")
                .header("Authorization", "Bearer " + accessToken)
                .retrieve()
                .bodyToMono(KakaoUserInfoResponseDto.class)
                .block();

        Objects.requireNonNull(userInfo, "카카오 사용자 정보 응답이 null입니다.");
        log.info("[카카오 로그인] 카카오 사용자 정보(providerId: {})를 성공적으로 조회했습니다.", userInfo.getId());

        // 3. 표준 DTO로 변환하여 반환
        return OAuthUserInfoDto.builder()
                .provider(OAuthProvider.KAKAO)
                .providerId(String.valueOf(userInfo.getId()))
                .nickname(userInfo.getProperties().get("nickname"))
                .profileImage(userInfo.getProperties().get("profile_image"))
                .build();
    }
}