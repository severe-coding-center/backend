package com.Guard.Back.Domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 자동 로그인을 위한 Refresh Token을 저장하는 엔티티.
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 실제 Refresh Token의 값.
     */
    @Column(nullable = false, unique = true)
    private String tokenValue;

    /**
     * 이 토큰의 주인인 보호자.
     * 보호자가 로그인한 경우에만 값이 존재합니다.
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", unique = true, nullable = true)
    private User user;

    /**
     * 이 토큰의 주인인 피보호자.
     * 피보호자가 로그인한 경우에만 값이 존재합니다.
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "protected_user_id", unique = true)
    private ProtectedUser protectedUser;

    /**
     * Refresh Token 값을 갱신하는 메소드.
     * @param tokenValue 새로 발급된 Refresh Token 값
     */
    public void updateToken(String tokenValue) {
        this.tokenValue = tokenValue;
    }

    @Builder
    public RefreshToken(String tokenValue, User user, ProtectedUser protectedUser) {
        this.tokenValue = tokenValue;
        this.user = user;
        this.protectedUser = protectedUser;
    }
}