package com.Guard.Back.Config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.InputStream;

@Configuration
@Slf4j
public class FCMConfig {

    @PostConstruct
    public void initialize() {
        try {
            // [수정] FileInputStream 대신 ClassPathResource 사용 (Docker/Jar 호환)
            // src/main/resources/fcm-key.json 파일을 찾습니다.
            ClassPathResource resource = new ClassPathResource("fcm-key.json");

            // 파일이 실제로 존재하는지 확인 (선택 사항이지만 안전을 위해 추천)
            if (!resource.exists()) {
                log.error("[FCM 오류] 리소스 폴더에 fcm-key.json 파일이 없습니다!");
                return;
            }

            InputStream serviceAccount = resource.getInputStream();

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();

            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
                log.info("[FCM] Firebase 초기화 성공");
            }
        } catch (Exception e) {
            log.error("[FCM] Firebase 초기화 실패", e);
        }
    }
}