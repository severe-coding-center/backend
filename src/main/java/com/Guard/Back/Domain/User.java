// 2️⃣ User.java – 단일 provider + providerId 구조로 재설계
package com.Guard.Back.Domain;

import com.Guard.Back.Auth.OAuthProvider;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"provider", "providerId"}))
public class User {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * KAKAO / NAVER / GOOGLE / ...
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OAuthProvider provider;

    /**
     * 각 플랫폼에서 내려주는 고유 식별자.
     */
    @Column(nullable = false)
    private String providerId;

    @Column(nullable = false)
    private String nickname;

    private String email;
    private String profileImage;

    /** 기본값은 USER, 필요 시 ADMIN 등 */
    @Column(nullable = false)
    @Builder.Default
    private String role = "USER";

    @CreationTimestamp
    private LocalDateTime createdAt;
}