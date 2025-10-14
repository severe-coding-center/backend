package com.Guard.Back.Dto;

public record RegisterResponse(
        String accessToken,
        String refreshToken,
        String linkingCode
) {}