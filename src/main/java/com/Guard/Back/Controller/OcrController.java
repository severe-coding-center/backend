package com.Guard.Back.Controller;

import com.Guard.Back.Service.OcrService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/ocr") // OCR 관련 API 경로
@RequiredArgsConstructor
@Slf4j
public class OcrController {

    private final OcrService ocrService; // OCR+TTS 처리를 담당하는 서비스

    /**
     * 프론트엔드에서 이미지 파일을 받아 OCR+TTS 처리된 MP3 오디오 데이터를 반환
     * @param imageFile 프론트에서 'image'라는 키로 보낸 사진 파일
     * @return MP3 오디오 파일 (byte[]) 또는 텍스트 없음(204) 응답
     */
    @PostMapping("/upload")
    public ResponseEntity<byte[]> handleOcrUpload(@RequestParam("image") MultipartFile imageFile) {
        log.info("[OCR 컨트롤러] OCR+TTS 이미지 파일 수신: {}", imageFile.getOriginalFilename());

        // 파일이 비어있는지 확인
        if (imageFile.isEmpty()) {
            // 빈 파일 요청 시 400 Bad Request 응답 (에러 메시지는 GlobalExceptionHandler가 처리)
            // 혹은 여기서 간단한 메시지 반환도 가능 (하지만 일관성을 위해 예외 발생 권장)
            // return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("빈 이미지 파일입니다.".getBytes());
            // 400 상태 코드만 반환
            return ResponseEntity.badRequest().build();
        }

        // OcrService 호출 (RuntimeException 발생 가능)
        // OcrService 내부에서 IOException은 RuntimeException으로 변환
        byte[] audioData = ocrService.getAudioFromImage(imageFile);

        // OcrService가 null을 반환한 경우 (AI 서버가 204 응답)
        if (audioData == null) {
            log.info("[OCR 컨트롤러] 인식된 텍스트 없음 (204 No Content 응답)");
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        }

        // 오디오 데이터가 정상적으로 반환된 경우
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.valueOf("audio/mpeg")); // MP3 미디어 타입 설정
        headers.setContentLength(audioData.length); // 컨텐츠 길이 설정

        log.info("[OCR 컨트롤러] MP3 오디오 데이터를 프론트엔드로 반환합니다.");
        return new ResponseEntity<>(audioData, headers, HttpStatus.OK);

        // try-catch 블록 없음: RuntimeException 발생 시 GlobalExceptionHandler가 처리
    }
}