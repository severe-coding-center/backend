package com.Guard.Back.Dto;

import com.Guard.Back.Domain.EventType;
import java.time.LocalDateTime;

public record AlertLogDto(
        EventType eventType,
        String message,
        LocalDateTime eventTime
) {}