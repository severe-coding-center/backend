package com.Guard.Back.Config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value; // 👈 import 추가
import org.springframework.context.annotation.Configuration;
// import org.springframework.core.io.ClassPathResource; // 👈 삭제
import java.io.FileInputStream; // 👈 import 추가
import java.io.InputStream;

@Configuration
public class FCMConfig {
    // 💡 application.properties에 추가한 'fcm.key.path' 값을 주입받습니다.
    @Value("${fcm.key.path}")
    private String fcmKeyPath;

    @PostConstruct
    public void initialize() {
        try {
            // 💡 ClassPathResource 대신 FileInputStream으로 변경합니다.
            // ClassPathResource resource = new ClassPathResource("fcm-key.json");
            InputStream serviceAccount = new FileInputStream(fcmKeyPath); // 👈 이렇게 수정!

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();
            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}