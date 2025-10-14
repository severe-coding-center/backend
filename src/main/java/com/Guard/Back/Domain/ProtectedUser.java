package com.Guard.Back.Domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor // 💡 [추가] 모든 필드를 사용하는 생성자를 추가합니다.
public class ProtectedUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String deviceId;

    @Column(unique = true)
    private String linkingCode;

    @Builder
    public ProtectedUser(String deviceId, String linkingCode) {
        this.deviceId = deviceId;
        this.linkingCode = linkingCode;
    }
}