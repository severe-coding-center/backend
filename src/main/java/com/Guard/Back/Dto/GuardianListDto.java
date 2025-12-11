package com.Guard.Back.Dto;

public record GuardianListDto(
        Long id,
        String profileImage,
        String nickname,
        String providerId,
        String provider,
        int linkedProtectedUserCount,
        boolean isAlertActive
) {}