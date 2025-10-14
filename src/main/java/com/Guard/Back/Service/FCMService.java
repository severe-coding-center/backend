package com.Guard.Back.Service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class FCMService {
    public void sendPushNotification(String fcmToken, String title, String body) {
        if (fcmToken == null || fcmToken.isEmpty()) {
            log.warn("[FCM] FCM 토큰이 비어있어 메시지를 발송할 수 없습니다.");
            return;
        }

        Message message = Message.builder()
                .setToken(fcmToken)
                .setNotification(Notification.builder()
                        .setTitle(title)
                        .setBody(body)
                        .build())
                .build();

        try {
            String response = FirebaseMessaging.getInstance().send(message);
            log.info("[FCM] 성공적으로 푸시 알림을 보냈습니다. {}", response);
        } catch (Exception e) {
            log.error("[FCM] 푸시 알림 발송에 실패했습니다. Error: {}", e.getMessage());
        }
    }
}