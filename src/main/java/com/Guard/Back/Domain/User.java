package com.Guard.Back.Domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter // nickname, profileImage 업데이트를 위해 Setter 추가
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 소셜 로그인 정보 필드
    @Column(unique = true)
    private String email;

    @Column(nullable = false)
    private String nickname;

    private String profileImage;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OAuthProvider provider;

    @Column(nullable = false)
    private String providerId;

    @Builder
    public User(String email, String nickname, String profileImage, OAuthProvider provider, String providerId) {
        this.email = email;
        this.nickname = nickname;
        this.profileImage = profileImage;
        this.provider = provider;
        this.providerId = providerId;
    }
}