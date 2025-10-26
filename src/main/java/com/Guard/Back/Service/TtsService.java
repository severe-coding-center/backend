package com.Guard.Back.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@RequiredArgsConstructor
@Slf4j
public class TtsService {

    private final WebClient webClient;

    // application.properties에서 AI TTS 서버 주소를 주입받습니다.
    @Value("${ai.tts.server.url}")
    private String ttsServerUrl;

    /**
     * 텍스트를 AI 서버로 보내 MP3 음성 파일(byte 배열)로 변환합니다.
     * @param textToSpeak 프론트엔드에서 받은 음성 변환 요청 텍스트
     * @return AI 서버가 반환한 MP3 파일 (byte[])
     */
    public byte[] getSpeechFromText(String textToSpeak) {
        log.info("[TTS 서비스] AI TTS 서버({})로 텍스트 전송: {}", ttsServerUrl, textToSpeak);

        // 1. 프론트엔드 코드(NewsSection.tsx)와 동일하게 'multipart/form-data' 바디를 구성합니다.
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("text", textToSpeak);

        try {
            // 2. WebClient를 사용하여 AI 서버에 POST 요청을 보냅니다.
            byte[] audioData = webClient.post()
                    .uri(ttsServerUrl) // AI TTS 서버 주소
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(BodyInserters.fromMultipartData(body)) // Form 데이터 전송
                    .retrieve() // 응답을 받습니다.
                    .bodyToMono(byte[].class) // AI 서버는 MP3 파일의 raw bytes를 반환합니다.
                    .block(); // 비동기 응답을 동기적으로 기다립니다.

            log.info("[TTS 서비스] AI 서버로부터 오디오 데이터를 성공적으로 수신했습니다.");
            return audioData;

        } catch (Exception e) {
            log.error("[TTS 서비스] AI 서버 호출 중 오류 발생", e);
            throw new RuntimeException("AI TTS 서버 통신에 실패했습니다.", e);
        }
    }
}