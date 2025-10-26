package com.Guard.Back.Dto;

import com.Guard.Back.Domain.EventType;
import java.time.ZonedDateTime;

public record AlertLogDto(
        EventType eventType,
        String message,
        ZonedDateTime eventTime
) {}