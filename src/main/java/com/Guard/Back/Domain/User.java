package com.Guard.Back.Domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 보호자 정보를 저장하는 엔티티 클래스.
 * 휴대폰 번호를 식별자로 사용하며, 비밀번호 기반으로 인증합니다.
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "users") // 실제 DB에는 'users' 테이블로 생성됩니다.
public class User {

    /**
     * 사용자의 고유 식별자 (자동 생성).
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 보호자의 이름.
     */
    @Column(nullable = false)
    private String name;

    /**
     * 보호자의 휴대폰 번호.
     * 로그인 시 ID 역할을 하며, 중복될 수 없습니다.
     */
    @Column(nullable = false, unique = true)
    private String phoneNumber;

    /**
     * 보호자의 비밀번호.
     * BCrypt로 해싱된 값이 저장됩니다.
     */
    @Column(nullable = false)
    private String password;

    @Builder
    public User(String name, String phoneNumber, String password) {
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.password = password;
    }
}