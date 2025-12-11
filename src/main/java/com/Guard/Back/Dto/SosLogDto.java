package com.Guard.Back.Dto;
import java.time.LocalDateTime;

public record SosLogDto(
        Long id,
        String deviceId,
        LocalDateTime eventTime,
        double latitude,
        double longitude,
        String guardianName
) {}