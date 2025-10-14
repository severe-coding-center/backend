package com.Guard.Back.Domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum UserRole {
    GUARDIAN("ROLE_GUARDIAN", "보호자"),
    PROTECTED("ROLE_PROTECTED", "피보호자");

    private final String key;
    private final String title;
}