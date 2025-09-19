package com.Guard.Back.Domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 보호자(User)와 피보호자(ProtectedUser) 간의 연결 관계를 정의하는 엔티티.
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Relationship {

    /**
     * 관계의 고유 식별자 (자동 생성).
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 관계의 주체인 보호자.
     * User 엔티티를 참조합니다.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "guardian_id")
    private User guardian;

    /**
     * 관계의 대상인 피보호자.
     * ProtectedUser 엔티티를 참조합니다.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "protected_user_id")
    private ProtectedUser protectedUser;

    @Builder
    public Relationship(User guardian, ProtectedUser protectedUser) {
        this.guardian = guardian;
        this.protectedUser = protectedUser;
    }
}