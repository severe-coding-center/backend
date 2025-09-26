package com.Guard.Back.Domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class LocationLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 💡 [변경] 피보호자 엔티티와 직접 연결합니다.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "protected_user_id", nullable = false)
    private ProtectedUser protectedUser;

    @Column(nullable = false)
    private double latitude;

    @Column(nullable = false)
    private double longitude;

    private LocalDateTime recordedAt;
}