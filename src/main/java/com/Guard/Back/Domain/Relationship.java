// Relationship.java
package com.Guard.Back.Domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Relationship {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 관계의 주체 (보호자)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "guardian_id")
    private User guardian;

    // 관계의 대상 (피보호자)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "protected_user_id")
    private User protectedUser;

    @Builder
    public Relationship(User guardian, User protectedUser) {
        this.guardian = guardian;
        this.protectedUser = protectedUser;
    }
}