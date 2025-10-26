package com.Guard.Back.Domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 애플리케이션의 사용자 역할(Role)을 정의하는 Enum.
 * Spring Security와 연동하여 역할 기반 접근 제어에 사용됩니다.
 */
@Getter
@RequiredArgsConstructor
public enum UserRole {
    /*보호자 역할. Spring Security에서는 "ROLE_GUARDIAN"으로 인식됩니다.*/
    GUARDIAN("ROLE_GUARDIAN", "보호자"),

    /*피보호자 역할. Spring Security에서는 "ROLE_PROTECTED"로 인식됩니다.*/
    PROTECTED("ROLE_PROTECTED", "피보호자");

    /*Spring Security에서 사용하는 권한 Key. (e.g., "ROLE_GUARDIAN")*/
    private final String key;

    /*화면 등에 표시될 역할의 이름. (e.g., "보호자")*/
    private final String title;
}