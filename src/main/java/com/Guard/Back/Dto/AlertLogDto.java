package com.Guard.Back.Dto;

import com.Guard.Back.Domain.EventType;
import java.time.ZonedDateTime; // 👈 LocalDateTime에서 ZonedDateTime으로 import 변경

public record AlertLogDto(
        EventType eventType,
        String message,
        ZonedDateTime eventTime // 👈 여기도 ZonedDateTime으로 타입 변경
) {}