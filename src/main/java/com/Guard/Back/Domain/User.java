package com.Guard.Back.Domain;

import jakarta.persistence.*;
import lombok.*;

/*보호자(소셜 로그인 사용자)의 정보를 정의하는 엔티티.*/
@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "users")
public class User {

    /*보호자의 고유 식별자 (자동 생성).*/
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /*소셜 로그인 제공자로부터 받은 이메일 (선택 사항).*/
    @Column(unique = true)
    private String email;

    /*소셜 로그인 제공자로부터 받은 닉네임.*/
    @Column(nullable = false)
    private String nickname;

    /*소셜 로그인 제공자로부터 받은 프로필 이미지 URL (선택 사항).*/
    private String profileImage;

    /*소셜 로그인 제공자 타입 (e.g., KAKAO).*/
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OAuthProvider provider;

    /*소셜 로그인 제공자 내에서 사용자를 식별하는 고유 ID.*/
    @Column(nullable = false)
    private String providerId;

    /*푸시 알림을 위한 FCM 디바이스 토큰.*/
    @Column(unique = true)
    private String fcmToken;

    @Enumerated(EnumType.STRING)
    private UserRole role;

    @Builder
    public User(String email, String nickname, String profileImage, OAuthProvider provider, String providerId, UserRole role) {
        this.email = email;
        this.nickname = nickname;
        this.profileImage = profileImage;
        this.provider = provider;
        this.providerId = providerId;
        this.fcmToken = fcmToken;
        this.role = role;
    }
}