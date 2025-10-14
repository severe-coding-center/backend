package com.Guard.Back.Dto;
import java.time.LocalDateTime;

public record LocationRequest(
        double latitude,
        double longitude,
        LocalDateTime recordedAt
) {}