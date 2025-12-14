package com.Guard.Back.Domain;

import jakarta.persistence.*;
import lombok.*;

/**
 * 피보호자(앱 사용자)의 정보를 정의하는 엔티티.
 * 소셜 로그인을 사용하지 않고, 기기 고유 ID를 통해 식별
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
     */
    @Column(nullable = false, unique = true)
    private String deviceId;

    /**
     * 보호자와의 연동을 위해 사용되는 6자리 코드.
     * 보호자와 연결되면 null 가능
     */
    @Column(unique = true)
    private String linkingCode;

    // 지오펜스(집) 위도
    private Double homeLatitude;

    // 지오펜스(집) 경도
    private Double homeLongitude;

    // 지오펜스 반경 (미터 단위)
    private Integer geofenceRadius;

    // 현재 지오펜스 내부에 있는지 여부 (상태 관리용)
    @Column(nullable = false)
    private boolean isInsideGeofence = true; // 기본값은 '내부'로 설정

    @Builder
    public ProtectedUser(String deviceId, String linkingCode) {
        this.deviceId = deviceId;
        this.linkingCode = linkingCode;
    }
}