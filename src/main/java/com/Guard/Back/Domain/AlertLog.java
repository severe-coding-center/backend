package com.Guard.Back.Domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp; // 👈 import 추가
import java.time.ZonedDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class AlertLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "protected_user_id", nullable = false)
    private ProtectedUser protectedUser;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EventType eventType;

    private String message; // e.g., "SOS 호출이 있었습니다.", "안심 구역을 벗어났습니다."

    @CreationTimestamp
    @Column(updatable = false, nullable = false)
    private ZonedDateTime eventTime;

    // 이벤트 발생 당시의 위치 정보 (선택적)
    private Double latitude;
    private Double longitude;
}