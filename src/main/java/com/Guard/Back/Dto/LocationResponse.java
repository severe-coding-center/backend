package com.Guard.Back.Dto;
import java.time.LocalDateTime;

public record LocationResponse(
        double latitude,
        double longitude,
        LocalDateTime recordedAt
) {}