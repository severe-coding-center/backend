package com.Guard.Back.Domain;

import jakarta.persistence.*;
import lombok.*;

/**
 * 피보호자(앱 사용자)의 정보를 정의하는 엔티티.
 * 소셜 로그인을 사용하지 않고, 기기 고유 ID를 통해 식별됩니다.
 */
@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class ProtectedUser {

    /*피보호자의 고유 식별자 (자동 생성).*/
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 앱이 설치된 기기의 고유 ID.
     * 사용자를 식별하는 주요 수단입니다.
     */
    @Column(nullable = false, unique = true)
    private String deviceId;

    /**
     * 보호자와의 연동을 위해 사용되는 6자리 코드.
     * 보호자와 연결되면 null이 될 수 있습니다.
     */
    @Column(unique = true)
    private String linkingCode;

    @Builder
    public ProtectedUser(String deviceId, String linkingCode) {
        this.deviceId = deviceId;
        this.linkingCode = linkingCode;
    }
}