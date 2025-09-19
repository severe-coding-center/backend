package com.Guard.Back.Domain;

import jakarta.persistence.*;
import lombok.*;

/**
 * 피보호자 정보를 저장하는 엔티티 클래스.
 * 개인정보 없이 기기 고유 ID를 통해 식별됩니다.
 */
@Entity
@Getter
@Setter // 연동 후 linkingCode를 null로 변경하기 위해 Setter를 사용합니다.
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProtectedUser {

    /**
     * 피보호자의 고유 식별자 (자동 생성).
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 앱이 설치된 기기의 고유 ID.
     * 이 값을 통해 사용자를 식별하고 자동 로그인 처리합니다.
     */
    @Column(nullable = false, unique = true)
    private String deviceId;

    /**
     * 보호자와의 연동을 위한 일회성 6자리 코드.
     * 연동이 완료되면 null 값으로 변경됩니다.
     */
    @Column(unique = true)
    private String linkingCode;

    @Builder
    public ProtectedUser(String deviceId, String linkingCode) {
        this.deviceId = deviceId;
        this.linkingCode = linkingCode;
    }
}