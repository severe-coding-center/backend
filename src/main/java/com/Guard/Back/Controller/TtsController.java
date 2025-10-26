package com.Guard.Back.Controller;

import com.Guard.Back.Dto.TtsRequestDto;
import com.Guard.Back.Service.TtsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tts")
@RequiredArgsConstructor
@Slf4j
public class TtsController {

    private final TtsService ttsService;

    /**
     * 프론트엔드에서 텍스트를 받아 AI 서버에 TTS 요청을 보냅니다.
     * @param ttsRequest 텍스트가 담긴 DTO
     * @return MP3 오디오 파일 (byte[])
     */
    @PostMapping
    public ResponseEntity<byte[]> generateSpeech(@RequestBody TtsRequestDto ttsRequest) {
        log.info("[TTS 컨트롤러] TTS 요청 수신: {}", ttsRequest.getText());

        byte[] audioData = ttsService.getSpeechFromText(ttsRequest.getText());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.valueOf("audio/mpeg"));
        headers.setContentLength(audioData.length);

        log.info("[TTS 컨트롤러] MP3 오디오 데이터를 프론트엔드로 반환합니다.");
        return new ResponseEntity<>(audioData, headers, HttpStatus.OK);
    }
}