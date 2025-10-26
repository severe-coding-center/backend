package com.Guard.Back.Domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/*피보호자의 위치 기록을 저장하는 엔티티.*/
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class LocationLog {

    /*위치 기록의 고유 식별자 (자동 생성).*/
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 이 위치 기록의 주인인 피보호자.
     * ProtectedUser 엔티티를 참조합니다.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "protected_user_id", nullable = false)
    private ProtectedUser protectedUser;

    /*위도 정보.*/
    @Column(nullable = false)
    private double latitude;

    /*경도 정보.*/
    @Column(nullable = false)
    private double longitude;

    /*위치가 기록된 시간.*/
    private LocalDateTime recordedAt;
}