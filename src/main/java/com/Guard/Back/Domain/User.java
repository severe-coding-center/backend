// User.java
package com.Guard.Back.Domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter // 💡 연동 후 코드를 null로 바꿀 때 필요해서 추가
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "users")
public class User {
    // ... 기존 id, name, phoneNumber, userType, createdAt 필드는 동일 ...
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(nullable = false, unique = true, length = 20)
    private String phoneNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserType userType;

    // 💡 [추가] 피보호자에게 발급될 고유 연동 코드 (null 허용, 고유해야 함)
    @Column(unique = true)
    private String linkingCode;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @Builder
    public User(String name, String phoneNumber, UserType userType, String linkingCode) {
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.userType = userType;
        this.linkingCode = linkingCode; // 💡 빌더에 추가
    }
}