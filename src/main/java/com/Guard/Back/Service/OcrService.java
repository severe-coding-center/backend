package com.Guard.Back.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.io.IOException;

@Service
@RequiredArgsConstructor
@Slf4j
public class OcrService {
    private final WebClient webClient;
    // ❌ private final TtsService ttsService; // TtsService 주입 제거!

    @Value("${ai.ocr.server.url}") // application.properties의 '/ocr-read' 주소
    private String ocrServerUrl;

    /**
     * 이미지를 AI 서버(/ocr-read)로 보내 OCR 후 TTS 변환된 MP3 오디오 데이터를 받아옵니다.
     * @param imageFile 프론트엔드에서 받은 이미지 파일
     * @return MP3 오디오 데이터 (byte[]) 또는 텍스트 없음 시 null
     */
    // 💡 반환 타입 byte[] 유지 (최종 목표와 일치)
    public byte[] getAudioFromImage(MultipartFile imageFile) {
        log.info("[OCR+TTS 서비스] AI 서버({})로 이미지 전송 및 오디오 요청 시작", ocrServerUrl);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        try {
            body.add("image", new ByteArrayResource(imageFile.getBytes()) {
                @Override
                public String getFilename() {
                    // AI 서버가 받을 때 사용할 파일 이름
                    return imageFile.getOriginalFilename() != null ? imageFile.getOriginalFilename() : "image.jpg";
                }
            });
        } catch (IOException e) {
            log.error("[OCR 서비스] 이미지 파일 처리 중 오류 발생", e);
            throw new RuntimeException("이미지 파일 처리 중 오류가 발생했습니다.", e);
        }

        try {
            // 💡 AI 서버 '/ocr-read' 호출 후 바로 byte[] 받기
            byte[] audioData = webClient.post()
                    .uri(ocrServerUrl) // AI 서버 /ocr-read 엔드포인트
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(BodyInserters.fromMultipartData(body))
                    .retrieve()
                    // 204 (No Content) 응답 처리
                    .onStatus(status -> status.value() == 204,
                            response -> {
                                log.info("[OCR+TTS 서비스] AI 서버 응답: 텍스트 없음 (204)");
                                return Mono.empty(); // block() 시 null 반환
                            })
                    .bodyToMono(byte[].class) // MP3 오디오 데이터(byte 배열)로 바로 변환
                    .block(); // 비동기 응답 대기

            // ❌ OCR 결과 텍스트를 받아 TtsService를 호출하는 로직 제거!

            if (audioData != null) {
                log.info("[OCR+TTS 서비스] AI 서버로부터 오디오 데이터를 성공적으로 수신했습니다.");
            }
            return audioData; // null 또는 실제 오디오 데이터 반환

        } catch (Exception e) {
            log.error("[OCR+TTS 서비스] AI 서버 호출 중 오류 발생", e);
            throw new RuntimeException("AI 서버(/ocr-read) 통신에 실패했습니다.", e);
        }
    }

    // ❌ performOcr() 메소드 제거 (이제 필요 없음)
}